package skkserv.jisyo

import Jisyo.Entries
import TrieJisyo.Trie

final case class TrieJisyo(root: Trie[ConversionCandidates]) extends Jisyo {
  def convert(midashi: String): Option[String] = root get midashi.toList flatMap (_.get)
  def complete(midashi: String): Option[String] = None
}

object TrieJisyo {

  def apply(entries: Entries): TrieJisyo =
    TrieJisyo(Trie.fromEntries(entries))

  final case class Trie[V](value: Option[V], children: Map[Char, Trie[V]]) {
    def get(key: List[Char]): Option[V] =
      key match {
        case Nil                 => value
        case headChar :: tailKey => children get headChar flatMap (_ get tailKey)
      }
  }

  object Trie {
    def fromEntries(entries: Entries): Trie[ConversionCandidates] = {
      val children = entries
        .filter { case (midashi, _) => midashi.nonEmpty }
        .groupBy { case (midashi, _) => midashi.head }
        .transform { case (_, entries) => fromEntries(entries.map { case (midashi, c) => (midashi.tail, c) }) }

      Trie(value = entries get "", children)
    }
  }
}
