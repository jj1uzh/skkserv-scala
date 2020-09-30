package skkserv

import java.io.File
import scala.annotation.tailrec
import scala.io.{Codec, Source}
import scala.util.chaining.scalaUtilChainingOps
import scala.util.control.Exception.nonFatalCatch

import JisyoFile.{entryLinePtrn, isOkuriAri, binSearchEntry, Midashi, Entry}

/**
  * @param okuriAriEntries must be sorted in asc
  * @param okuriNasiEntries must be sorted in asc
  */
final class JisyoFile(okuriAriEntries: Vector[Entry], okuriNasiEntries: Vector[Entry]) {
  def convert(midashi: Midashi): Vector[String] = {
    val searchResult = for {
      entry <- binSearchEntry(
        midashi,
        if (isOkuriAri(midashi)) okuriAriEntries else okuriNasiEntries)
      entryLinePtrn(_, cands) = entry
    } yield (cands split '/').toVector

    searchResult getOrElse Vector.empty
  }
}

object JisyoFile {

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

  val entryLinePtrn = """(\S+)\s+/(.*)/""".r
  type Midashi = String /* should be opaque type in scala 3 */
  type Entry = String

  def isOkuriAri(midashi: Midashi): Boolean =
      ('a' to 'z') contains midashi.last

  @inline private def extractMidashi(entry: Entry): String =
    entry takeWhile (_ != ' ')

  @inline private def compareMidashies(m1: Midashi, m2: Midashi): Int = {
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

  /**
    * @param midashi
    * @param lines must be sorted in asc
    * @return
    */
  def binSearchEntry(midashi: Midashi, entries: Vector[Entry]): Option[Entry] = {
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
}
