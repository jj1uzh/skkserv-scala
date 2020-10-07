package skkserv

import java.io.File
import scala.annotation.tailrec
import scala.io.{Codec, Source}
import scala.util.chaining.scalaUtilChainingOps
import scala.util.control.Exception.nonFatalCatch

import JisyoFile.{Midashi, Entry, extractMidashi, compareMidashies}

/**
  * @param okuriAriEntries must be sorted in asc
  * @param okuriNasiEntries must be sorted in asc
  */
final class JisyoFile(okuriAriEntries: Vector[Entry], okuriNasiEntries: Vector[Entry]) {

  /**
    * @param midashi to convert
    * @return Some if there are one or more candidates, otherwise None
    */
  def convert(midashi: Midashi): Option[Vector[String]] =
    for {
      entry <- binSearchEntry(midashi)
    } yield extractCandidates(entry)

  /**
    * @param midashi to search for
    * @param lines must be sorted in asc
    * @return Some if found, otherwise None
    */
  private def binSearchEntry(midashi: Midashi): Option[Entry] = {
    val entries = if (isOkuriAri(midashi)) okuriAriEntries else okuriNasiEntries
    
    @tailrec def _impl(range: Range): Option[String] =
      range.size match {
        case 0 => None
        case len => {
          val idx = len / 2 + range.start
          compareMidashies(midashi, entries(idx) pipe extractMidashi) match {
            case 0          => Some(entries(idx))
            case c if c < 0 => _impl(range.start until idx)
            case c if c > 0 => _impl(idx + 1 until range.end)
          }
        }
      }

    _impl(entries.indices)
  }

  @inline private def extractCandidates(entry: Entry): Vector[String] =
    (entry dropWhile (_ != ' ') drop 2 dropRight 1 split '/').toVector

  @inline private def isOkuriAri(midashi: Midashi): Boolean = {
    midashi.length match {
      case 1 => false
      case len =>
        ('a' to 'z' contains midashi.last) &&
        !('a' to 'z' contains midashi(len - 2))
    }
  }
}

object JisyoFile {

  type Midashi = String /* should be opaque type in scala 3 */
  type Entry = String
  type LoadResult = Either[String, JisyoFile]

  def fromFile(path: String)(implicit codec: Codec): LoadResult = {
    def isAsc(entries: Vector[Entry]): Boolean = {
      entries.length match {
        case 0 | 1 => true
        case _ => {
          val (m0, m1) = (extractMidashi(entries(0)), extractMidashi(entries(1)))
          compareMidashies(m0, m1) < 0
        }
      }
    }

    nonFatalCatch
      .withApply (e => Left(s"[error] failed to load jisyo file ${path}: ${e.getMessage()}"))
      .apply {
        val src = Source fromFile new File(path)
        val lines = src.getLines().toVector
        val okuriAriSignIndex = lines indexOf ";; okuri-ari entries."
        assert(okuriAriSignIndex != -1)
        val okuriNasiSignIndex = lines indexOf ";; okuri-nasi entries."
        assert(okuriNasiSignIndex != -1)

        val okuriAriEntries = lines.slice(okuriAriSignIndex, okuriNasiSignIndex)
          .filterNot(_ startsWith ";;")
          .pipe(es => if (isAsc(es)) es else es.reverse)
        val okuriNasiEntries = lines.slice(okuriNasiSignIndex, lines.length)
          .filterNot(_ startsWith ";;")
          .pipe(es => if (isAsc(es)) es else es.reverse)

        Right(new JisyoFile(okuriAriEntries, okuriNasiEntries))
      }
  }

  @inline def extractMidashi(entry: Entry): String =
    entry takeWhile (_ != ' ')

  @inline def compareMidashies(m1: Midashi, m2: Midashi): Int = {
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
      
    _impl(m1.toList, m2.toList)
  }
}
