package skkserv.jisyo

import scala.io.Source
import scala.util.{Try, Using}

trait Jisyo {

  /**
    * 見出し語を変換
    * @param midashi
    * @return /で区切った変換候補
    */
  def convert(midashi: String): Option[String]

  /**
    * 見出し語を補完
    * @param midashi
    * @return /で区切った補完候補
    */
  def complete(midashi: String): Option[String]
}

object Jisyo {

  /**
    * 見出し語→候補のMap
    */
  type Entries = Map[String, ConversionCandidates] // TODO: Entriesをクラスにする
  val emptyEntries = Map[String, ConversionCandidates]()

  private val entryLinePattern = """([\S]+)\s+/(.*)/""".r
  private val ignoringLinePattern = """;;.*|\s*""".r

  /**
    * sourceからEntriesを作成
    *
    * @param sourceName sourceの名前。エラー表示に必要
    * @param source 入力ソース
    * @return
    */
  def entriesFromSource(sourceName: String, source: Source): Entries =
    source
      .getLines()
      .zipWithIndex
      .map {
        case (entryLinePattern(midashi, candidates), _) => Some(midashi -> ConversionCandidates(candidates))
        case (ignoringLinePattern(), _)                 => None
        case (invalidLine, index) =>
          println(s"""WARN: Ignored jisyo entry in "$sourceName", line ${index + 1} """); None
      }
      .flatten
      .toMap

  /**
    * 辞書ファイルを読込む
    *
    * @param path ファイルパス
    * @return
    */
  def entriesFromFile(path: String): Try[Entries] =
    Using(Source.fromFile(path, "EUC-JP"))(entriesFromSource(sourceName = path, _))

  /**
    * ふたつのEntriesを結合する
    *
    * @param e1
    * @param e2
    * @return
    */
  def merge(e1: Entries, e2: Entries): Entries = {
    val keys = e1.keySet ++ e2.keySet
    keys.map { key =>
      val candidates = ((e1.get(key), e2.get(key)): @unchecked) match {
        case (Some(l), Some(r)) => l append r
        case (Some(l), _)       => l
        case (_, Some(r))       => r
      }
      key -> candidates
    }.toMap
  }
}
