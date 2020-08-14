package skkserv.jisyo

import Jisyo.Entries

final case class MapJisyo(entries: Entries) extends Jisyo {
  def convert(midashi: String): Option[String] = entries get midashi flatMap (_.get)
  def complete(midashi: String): Option[String] = None
}
