package skkserv.jisyo

object MapJisyo {

  implicit object MapIsStaticJisyo extends StaticJisyo[Map[String, String]] {

    def fromEntries(entries: Map[String, String]): Map[String, String] = entries

    def convert(jisyo: Map[String, String])(midashi: String): Option[String] = jisyo get midashi

    def complete(jisyo: Map[String, String])(midashi: String): Option[String] = None
  }
}
