package skkserv.jisyo

import scala.io.Source
import scala.util.{Try, Using}

/**
  * 辞書。変換と補完ができる
  */
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
  type StaticEntries = Map[String, String]

  /**
    * 見出し語→動的候補のMap
    */
  type DynamicEntries = Map[String, () => String]

  private val entryLinePattern = """([\S]+)\s+/(.*)/""".r
  private val ignoringLinePattern = """;;.*|\s*""".r

  /**
    * sourceからEntriesを作成
    *
    * @param sourceName sourceの名前。エラー表示に必要
    * @param source 入力ソース
    * @return
    */
  def entriesFromSource(sourceName: String, source: Source): StaticEntries =
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
    * 辞書ファイルを読込む
    *
    * @param path ファイルパス
    * @return
    */
  def entriesFromFile(path: String): Try[StaticEntries] =
    Using(Source.fromFile(path, "EUC-JP"))(entriesFromSource(sourceName = path, _))
}
