package skkserv.jisyo

import scala.io.Source
import scala.util.{Try, Using}

sealed trait Jisyo {

  /**
    * 見出し語を変換する。
    *
    * @param midashi 見出し語
    * @return 変換候補を/で区切った文字列
    */
  def convert(midashi: String): Option[String]

  /**
    * 見出し語を補完する。
    *
    * @param midashi 見出し語
    * @return 補完候補を/で区切った文字列
    */
  def complete(midashi: String): Option[String]
}

final case class StaticJisyo(private val entries: Map[String, String]) extends Jisyo {
  def convert(midashi: String): Option[String] = entries get midashi
  def complete(midashi: String): Option[String] = None // 未実装
}

object StaticJisyo {
  private val entryLinePattern = """([\S]+)\s+/(.*)/""".r
  private val commentLinePattern = """;;.*""".r
  private val emptyLinePattern = """\s*""".r

  /**
    * sourceから静的辞書を作成する。
    *
    * @param sourceName sourceの名前。エラー表示に必要。
    * @param source 入力ソース
    * @return
    */
  def apply(sourceName: String, source: Source): StaticJisyo = {
    val entries = source.getLines().zipWithIndex map {
      case (entryLinePattern(midashi, candidates), _) => Some(midashi -> candidates)
      case (commentLinePattern(), _)                  => None
      case (emptyLinePattern(), _)                    => None
      case (invalidLine, index) =>
        println(s"""WARN: Ignored jisyo entry in "$sourceName", line ${index + 1} """); None
    }

    StaticJisyo(entries.flatten.toMap)
  }

  /**
    * 辞書ファイルを読込む。
    *
    * @param path ファイルパス
    * @return
    */
  def fromFile(path: String): Try[StaticJisyo] =
    Using(Source.fromFile(path, "EUC-JP"))(apply(sourceName = path, _))

  /**
    * 静的辞書を結合する。
    *
    * @param jisyoes 結合する辞書のリスト
    * @return
    */
  def merge(jisyoes: List[StaticJisyo]): StaticJisyo = {
    val mappings = for (jisyo <- jisyoes) yield jisyo.entries map {
      case (midashi, candidates) => (midashi, candidates.split("/"))
    }
    val midashiSet = jisyoes map (_.entries) map (_.keySet) reduce (_ ++ _)
    val entries =
      midashiSet.map(midashi => (midashi -> mappings.map(_ get midashi).flatten.reduce(_ ++ _).distinct.mkString("/")))
    StaticJisyo(entries.toMap)
  }
}
