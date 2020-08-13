package skkserv.jisyo

import scala.io.Source
import scala.util.{Try, Using}

/**
  * 静的辞書の型クラス
  */
trait StaticJisyo[T] {

  /**
    * 見出し→候補 のMapから生成
    *
    * @param entries
    * @return
    */
  def fromEntries(entries: Map[String, String]): T

  /**
    * @param jisyo
    * @param midashi
    * @return /で区切った変換候補
    */
  def convert(jisyo: T)(midashi: String): Option[String]

  /**
    * @param jisyo
    * @param midashi
    * @return /で区切った補完候補
    */
  def complete(jisyo: T)(midashi: String): Option[String]
}

object StaticJisyo {

  /**
    * 見出し語→候補のMap
    */
  type Entries = Map[String, String]

  private val entryLinePattern = """([\S]+)\s+/(.*)/""".r
  private val ignoringLinePattern = """;;.*|\s*""".r

  /**
    * sourceからEntriesを作成する。
    *
    * @param sourceName sourceの名前。エラー表示に必要。
    * @param source 入力ソース
    * @return
    */
  def entriesFromSource(sourceName: String, source: Source): Entries =
    source
      .getLines()
      .zipWithIndex
      .map {
        case (entryLinePattern(midashi, candidates), _) => Some(midashi -> candidates)
        case (ignoringLinePattern(), _)                 => None
        case (invalidLine, index) =>
          println(s"""WARN: Ignored jisyo entry in "$sourceName", line ${index + 1} """); None
      }
      .flatten
      .toMap

  /**
    * 辞書ファイルを読込む。
    *
    * @param path ファイルパス
    * @return
    */
  def entriesFromFile(path: String): Try[Entries] =
    Using(Source.fromFile(path, "EUC-JP"))(entriesFromSource(sourceName = path, _))
}
