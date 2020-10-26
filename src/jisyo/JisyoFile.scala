package skkserv.jisyo

import scala.annotation.tailrec
import scala.io.{Codec, Source}
import scala.util.chaining.scalaUtilChainingOps
import scala.util.control.Exception.nonFatalCatch

import JisyoFile.{Midashi, Entry}

/** @param okuriAriEntries must be sorted in asc
  * @param okuriNasiEntries must be sorted in asc
  */
final class JisyoFile(okuriAriEntries: Vector[Entry], okuriNasiEntries: Vector[Entry]) {

  /** @param midashi
    * @return Some if there are one or more candidates, otherwise None
    */
  def convert(midashi: String): Option[Vector[String]] =
    for (entry <- binarySearch(Midashi(midashi))) yield entry.candidates

  /** @param startsWith
    * @return Some if there are one or more completions, otherwise None
    */
  def complete(startsWith: String): Option[Vector[String]] =
    for (midashies <- collectStartsWith(startsWith, okuriNasiEntries)) yield midashies.map(_.value)

  private def binarySearch(midashi: Midashi): Option[Entry] = {
    val entries = if (midashi.isOkuriAri) okuriAriEntries else okuriNasiEntries
    @tailrec def _impl(range: Range): Option[Entry] =
      range.size match {
        case 0 => None
        case len =>
          val idx = len / 2 + range.start
          midashi <=> entries(idx).midashi match {
            case 0          => Some(entries(idx))
            case c if c < 0 => _impl(range.start until idx)
            case c if c > 0 => _impl(idx + 1 until range.end)
          }
      }
    _impl(entries.indices)
  }

  private def collectStartsWith(str: String, entries: Vector[Entry]): Option[Vector[Midashi]] = {
    for (pivotIndex <- binarySearchStartsWith(str, entries)) yield {
      val view = entries.view
      val forwardEntries = view.slice(0, pivotIndex).reverse.takeWhile(_.value.startsWith(str)).map(_.midashi).toVector
      val backwardEntries = view.slice(pivotIndex, view.length).takeWhile(_.value.startsWith(str)).map(_.midashi).toVector
      forwardEntries.reverse ++ backwardEntries
    }
  }

  private def binarySearchStartsWith(str: String, entries: Vector[Entry]): Option[Int] = {
    @tailrec def _impl(range: Range): Option[Int] = {
      range.size match {
        case 0 => None
        case len =>
          val idx = len / 2 + range.start
          str ^<=>: entries(idx).midashi match {
            case 0 => Some(idx)
            case c if c < 0 => _impl(range.start until idx)
            case c if c > 0 => _impl(idx + 1 until range.end)
          }
      }
    }
    _impl(entries.indices)
  }
}

object JisyoFile {

  /** Load a SKK jisyo file from path
    * @param path
    * @param codec
    * @return Right if successfully loaded, otherwise Left with error messages
    */
  def fromFile(path: String): Either[String, JisyoFile] = {

    def isAsc(entries: Vector[Entry]): Boolean = {
      entries.length match {
        case 0 | 1 => true
        case _ => entries(0).midashi <=> entries(1).midashi < 0
      }
    }

    def impl(codec: Codec): Either[String, JisyoFile] =
      nonFatalCatch
        .withApply { e => Left(
          s"[warn] failed to load jisyo file ${path}: ${e.getClass.getName()}: ${e.getMessage()}"
        )}
        .apply {
          val src = Source.fromFile(path)(codec)
          val lines = src.getLines().toVector
          val okuriAriSignIndex = lines indexOf ";; okuri-ari entries."
          assert(okuriAriSignIndex != -1)
          val okuriNasiSignIndex = lines indexOf ";; okuri-nasi entries."
          assert(okuriNasiSignIndex != -1)

          @inline def extract: Vector[String] => Vector[Entry] =
            _ filterNot (_ startsWith ";;") map Entry pipe (es => if (isAsc(es)) es else es.reverse)

          val okuriAriEntries = lines.slice(okuriAriSignIndex, okuriNasiSignIndex) pipe extract
          val okuriNasiEntries = lines.slice(okuriNasiSignIndex, lines.length) pipe extract

          Right(new JisyoFile(okuriAriEntries, okuriNasiEntries))
        }

    impl(Codec("euc-jp")) orElse impl(Codec("utf-8")) // tmp impl
  }

  private val lowerAlphabets = 'a' to 'z'
  final case class Midashi(value: String) extends AnyVal {

    def isOkuriAri: Boolean =
      value.length match {
        case 1 => false
        case len =>
          (lowerAlphabets contains value.last) && !(lowerAlphabets contains value(len - 2))
      }

    def compare(that: Midashi): Int = {
      def _impl(m1: List[Char], m2: List[Char]): Int =
        (m1, m2) match {
          case (Nil, Nil) => 0
          case (Nil, _)   => -1
          case (_, Nil)   => 1
          case (h1 :: t1, h2 :: t2) => (h1 compareTo h2) match {
            case 0 => _impl(t1, t2)
            case c => c
          }
        }
      _impl(value.toList, that.value.toList)
    }

    @inline def <=> = compare _

    /** prefix comparing
      * @param str prefix
      * @return 0 if this begins with str, otherwise comparing number
      */
    def ^<=>:(str: String): Int = {
      def _impl(pre: List[Char], m: List[Char]): Int =
        (pre, m) match {
          case (Nil, _) => 0
          case (_, Nil) => 1
          case (preHead::preTail, mHead::mTail) =>
            (preHead compareTo mHead) match {
              case 0 => _impl(preTail, mTail)
              case c => c.sign
            }
        }
      _impl(str.toList, this.value.toList)
    }
  }

  final case class Entry(value: String) extends AnyVal {
    def midashi: Midashi =
      Midashi(value takeWhile (_ != ' '))

    def candidates: Vector[String] =
      (value dropWhile (_ != ' ') drop 2 dropRight 1 split '/').toVector
  }
}
