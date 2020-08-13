package skkserv.jisyo

import Jisyo.StaticEntries
import TrieJisyo.Trie

final case class StaticTrieJisyo(root: Trie[String]) extends Jisyo {
  def convert(midashi: String): Option[String] = root get midashi.toList
  def complete(midashi: String): Option[String] = None
}

final case class DynamicTrieJisyo(root: Trie[() => String]) extends Jisyo {
  def convert(midashi: String): Option[String] = root get midashi.toList map (_())
  def complete(midashi: String): Option[String] = None
}

object TrieJisyo {

  final case class Trie[V](value: Option[V], children: Map[Char, Trie[V]]) {
    def get(key: List[Char]): Option[V] =
      key match {
        case Nil                 => value
        case headChar :: tailKey => children get headChar flatMap (_ get tailKey)
      }
  }

  object Trie {
    def fromEntries(entries: StaticEntries): Trie[String] = {
      val children = entries
        .filter { case (midashi, _) => midashi.nonEmpty }
        .groupBy { case (midashi, _) => midashi.head }
        .transform { case (_, entries) => fromEntries(entries.map { case (midashi, c) => (midashi.tail, c) }) }

      Trie(value = entries get "", children)
    }
  }
}
