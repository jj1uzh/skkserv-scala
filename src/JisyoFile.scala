package skkserv

import java.io.File
import scala.io.{Codec, Source}
import scala.util.chaining.scalaUtilChainingOps
import scala.util.control.Exception.nonFatalCatch

final case class JisyoFile(file: File, okuriAriRange: Option[Range], okuriNasiRange: Option[Range], isDesc: Boolean) {
  import JisyoFile.{entryLinePtrn, isOkuriAri}

  def convert(midashi: String)(implicit codec: Codec): Vector[String] =
    ( for {
      searchRange <- if (isOkuriAri(midashi)) okuriAriRange else okuriNasiRange
      //entry <- binSearchEntry(midashi, this.file, searchRange, isDesc)
      searchStr = s"$midashi "
      entry <- (Source fromFile this.file).getLines().slice(searchRange.start, searchRange.end) find (_ startsWith searchStr)
      entryLinePtrn(_, cands) = entry
    } yield (cands split '/').toVector
    ) getOrElse Vector.empty
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

      Right(JisyoFile(file, okuriAriRange, okuriNasiRange, isDesc = true /* TODO: desc/asc */ ))
    }
  }

  val entryLinePtrn = """(\S+)\s+/(.*)/""".r

  def isOkuriAri(midashi: String): Boolean =
    ('a' to 'z') contains midashi.last
}
