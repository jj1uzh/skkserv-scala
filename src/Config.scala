package skkserv

import Config.DynamicJisyoes

case class Config(
    jisyoType: String = "hash",
    dynamicJisyoes: DynamicJisyoes = DynamicJisyoes(),
    jisyoDir: String = """/usr/share/skk""",
    jisyoFiles: Seq[String] = Nil
)

object Config {

  case class DynamicJisyoes(
      datetime: Boolean = true
  )
}
