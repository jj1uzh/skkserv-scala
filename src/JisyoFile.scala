package skkserv

import java.io.File
import scala.io.{Codec, Source}
import scala.util.chaining.scalaUtilChainingOps
import scala.util.control.Exception.nonFatalCatch
import scala.annotation.tailrec

final case class JisyoFile(file: File, okuriAriRange: Option[Range], okuriNasiRange: Option[Range], isDesc: Boolean) {
  import JisyoFile.{binSearchEntry, entryLinePtrn, isOkuriAri}

  def convert(midashi: String)(implicit codec: Codec): Vector[String] =
    (
      for {
        searchRange <- if (isOkuriAri(midashi)) okuriAriRange else okuriNasiRange
        lines = (Source fromFile file).getLines().slice(searchRange.start, searchRange.end).toVector
        result <- binSearchEntry(midashi, lines, isDesc)
      } yield result
    ) map {
      case entryLinePtrn(_, cands) => (cands split '/').toVector
    } getOrElse Vector.empty
}

object JisyoFile {

  type LoadResult = Either[String, JisyoFile]
  def fromFile(path: String)(implicit codec: Codec): LoadResult = {
    nonFatalCatch withApply (e => Left(s"[error] failed to load jisyo file ${path}: ${e.getMessage()}")) apply {
      val file = new File(path)
      val src = Source fromFile file
      val lines = src.getLines().toArray
      val okuriAriSignIndex = lines indexOf ";; okuri-ari entries."
      assert(okuriAriSignIndex != -1)
      val okuriNasiSignIndex = lines indexOf ";; okuri-nasi entries."
      assert(okuriNasiSignIndex != -1)

      val okuriAriRange =
        if (okuriNasiSignIndex == okuriAriSignIndex + 1) None
        else Some((okuriAriSignIndex + 1) until okuriNasiSignIndex)

      val okuriNasiRange =
        if (lines.length == okuriAriSignIndex + 1) None
        else Some((okuriNasiSignIndex + 1) until lines.length)

      Right(JisyoFile(file, okuriAriRange, okuriNasiRange, isDesc = false /* TODO: desc/asc */ ))
    }
  }

  val entryLinePtrn = """(\S+)\s+/(.*)/""".r

  def isOkuriAri(midashi: String): Boolean =
    ('a' to 'z') contains midashi.last

  @inline private def extractMidashi(entry: String): String =
    entry takeWhile (_ != ' ')

  @inline private def compareMidashies(m1: String, m2: String): Int = {
    def _impl(m1: List[Char], m2: List[Char]): Int =
      (m1, m2) match {
        case (Nil, Nil) => 0
        case (Nil, _)   => -1
        case (_, Nil)   => 1
        case (h1 :: t1, h2 :: t2) =>
          (h1 compareTo h2) match {
            case 0 => _impl(t1, t2)
            case c => c
          }
      }
    _impl(m1.toList, m2.toList)
  }

  def binSearchEntry(midashi: String, lines: Vector[String], isDesc: Boolean): Option[String] = {
    val ordCoef = if (isDesc) 1 else -1
    @tailrec def _impl(range: Range): Option[String] =
      range.size match {
        case 0 => None
        case len => {
          val idx = len / 2 + range.start
          (compareMidashies(midashi, extractMidashi(lines(idx))) * ordCoef) match {
            case 0          => Some(lines(idx))
            case c if c < 0 => _impl(range.start until idx)
            case c if c > 0 => _impl(idx + 1 until range.end)
          }
        }
      }
    _impl(lines.indices)
  }
}
