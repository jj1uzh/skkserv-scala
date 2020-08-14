package skkserv.jisyo

final case class ConversionCandidates(
    private val dynamic: Seq[() => String],
    private val static: Option[String]
) {

  def get: Option[String] = {
    dynamic match {
      case Nil => static
      case d   => Some(d.map(_.apply).mkString("/") concat (static.map('/' +: _) getOrElse ""))
    }
  }

  def append(that: ConversionCandidates): ConversionCandidates = {
    val newStatic = (this.static, that.static) match {
      case (Some(l), Some(r)) => Some(s"$l/$r")
      case (l, None)          => l
      case (_, r)             => r
    }
    ConversionCandidates(this.dynamic ++ that.dynamic, newStatic)
  }
}

object ConversionCandidates {
  def apply(dynamic: Seq[() => String]): ConversionCandidates =
    ConversionCandidates(dynamic, None)

  def apply(static: String): ConversionCandidates = {
    val s = if (static.isEmpty) None else Some(static)
    ConversionCandidates(Seq.empty, s)
  }
}
