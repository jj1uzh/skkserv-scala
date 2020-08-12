package skkserv.jisyo

/**
  * 木構造の辞書
  */
trait Jisyo[T] {

  /**
    * 変換候補
    */
  protected val conversionCandidate: Option[T]

  /**
    * 次の文字->辞書のmap
    */
  protected val nexts: Map[Char, Jisyo[T]]

  /**
    * 変換候補を/で区切った文字列で返す
    */
  protected def get: Option[String]
}

final case class DynamicJisyo(
    protected val conversionCandidate: Option[() => String],
    protected val nexts: Map[Char, Jisyo[() => String]]
) extends Jisyo[() => String] {
  protected def get = conversionCandidate.map(_())
}

object Jisyo {

  /**
    * 辞書を使って見出し語を変換
    *
    * @param jisyo 辞書
    * @param midashi 見出し語
    * @return 変換結果
    */
  def convert[T](jisyo: Jisyo[T])(midashi: List[Char]): Option[String] =
    midashi match {
      case Nil          => jisyo.get
      case head :: tail => jisyo.nexts.get(head).flatMap(convert(_)(tail))
    }

  def complete[T](jisyo: Jisyo[T])(midashi: List[Char]): Option[String] =
    None
}
