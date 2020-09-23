package skkserv

import java.io.File
import scala.io.{Codec, Source}
import scala.util.chaining.scalaUtilChainingOps
import scala.util.Try
import scala.util.Using.Releasable

final case class JisyoFile(file: File, okuriAriRange: Range, okuriNasiRange: Range) {
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

  def fromFile(path: String)(implicit codec: Codec): JisyoFile = {
    val file = new File(path)
    val src = Source fromFile file
    val lines = src.getLines()
    val okuriAriSignIndex = lines indexOf ";; okuri-ari entries."
    val okuriNasiSignIndex = lines indexOf ";; okuri-nasi entries."
    val okuriAriRange = (okuriAriSignIndex + 1) until okuriNasiSignIndex
    val okuriNasiRange = (okuriNasiSignIndex + 1) until lines.length
    JisyoFile(file, okuriAriRange, okuriNasiRange)
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
