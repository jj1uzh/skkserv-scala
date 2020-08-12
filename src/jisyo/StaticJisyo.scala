package skkserv.jisyo

import scala.io.Source
import scala.util.{Try, Using}

final case class StaticJisyo(
    protected val conversionCandidate: Option[String],
    protected val nexts: Map[Char, Jisyo[String]]
) extends Jisyo[String] {
  protected def get = conversionCandidate
}

object StaticJisyo {
  private val entryLinePattern = """([\S]+)\s+/(.*)/""".r
  private val ignoringLinePattern = """;;.*|\s*""".r

  /**
    * @param entries 見出し->変換候補のMap
    * @return
    */
  def apply(entries: Map[String, String]): StaticJisyo = {
    val grouped = entries
      .filter { case (midashi, _) => midashi.nonEmpty }
      .groupBy { case (midashi, _) => midashi.head }

    StaticJisyo(
      entries get "",
      grouped.transform {
        case (_, entries) => StaticJisyo(entries.map { case (midashi, candidates) => (midashi.tail, candidates) })
      }
    )
  }

  /**
    * sourceから静的辞書を作成する。
    *
    * @param sourceName sourceの名前。エラー表示に必要。
    * @param source 入力ソース
    * @return
    */
  def fromSource(sourceName: String, source: Source): StaticJisyo = {
    val entries = source
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

    StaticJisyo(entries)
  }

  /**
    * 辞書ファイルを読込む。
    *
    * @param path ファイルパス
    * @return
    */
  def fromFile(path: String): Try[StaticJisyo] =
    Using(Source.fromFile(path, "EUC-JP"))(fromSource(sourceName = path, _))
}
