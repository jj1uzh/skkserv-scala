package skkserv.jisyo

object TrieJisyo {

  final case class TrieNode[K, V](value: Option[V], children: Map[K, TrieNode[K, V]])

  implicit object TrieIsStaticJisyo extends StaticJisyo[TrieNode[Char, String]] {

    def complete(jisyo: TrieNode[Char, String])(midashi: String): Option[String] = None

    def convert(jisyo: TrieNode[Char, String])(midashi: String): Option[String] =
      convertImpl(jisyo)(midashi.toSeq)

    private def convertImpl(jisyo: TrieNode[Char, String])(chars: Seq[Char]): Option[String] =
      chars match {
        case Nil          => jisyo.value
        case head :: tail => jisyo.children.get(head).flatMap(convertImpl(_)(tail))
      }

    def fromEntries(entries: Map[String, String]): TrieNode[Char, String] = {
      val grouped = entries
        .filter { case (midashi, _) => midashi.nonEmpty }
        .groupBy { case (midashi, _) => midashi.head }

      TrieNode(
        entries get "",
        grouped.transform {
          case (_, entries) =>
            fromEntries(entries.map { case (midashi, candidates) => (midashi.tail, candidates) })
        }
      )
    }
  }
}
