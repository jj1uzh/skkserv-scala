package skkserv

import java.io.File
import scala.io.{Codec, Source}
import scala.util.chaining.scalaUtilChainingOps
import scala.util.control.Exception.nonFatalCatch

final case class JisyoFile(file: File, okuriAriRange: Option[Range], okuriNasiRange: Option[Range]) {
  import skkserv.JisyoFile.Candidates

  def convert(midashi: String)(implicit codec: Codec): Option[Candidates] = {
    val lines = Source.fromFile(file).getLines()
    for {
      entry <- lines find (_ startsWith midashi)
      candidates = entry.splitAt(entry indexOf ' ')._2 pipe Candidates
    } yield candidates
  }
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

      Right(JisyoFile(file, okuriAriRange, okuriNasiRange))
    }
  }

  final case class Candidates(value: String) extends AnyVal {
    def split: Array[String] = value drop 1 dropRight 1 split '/'
  }

  object Implicits {

    implicit class OptionCandidatesOperators(optCands: Option[Candidates]) {
      def concat(another: Option[Candidates]): Option[Candidates] =
        (optCands, another) match {
          case (Some(cands0), Some(cands1)) =>
            (cands0.split ++ cands1.split).distinct.mkString("/", "/", "/") pipe Candidates pipe Option.apply
          case (c @ Some(_), None) => c
          case (None, c)           => c
        }

      @inline def ++ = this concat _
    }
  }
}
