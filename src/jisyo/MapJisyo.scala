package skkserv.jisyo

import Jisyo.{StaticEntries, DynamicEntries}

final case class StaticMapJisyo(entries: StaticEntries) extends Jisyo {
  def convert(midashi: String): Option[String] = entries get midashi
  def complete(midashi: String): Option[String] = None
}

final case class DynamicMapJisyo(entries: DynamicEntries) extends Jisyo {
  def convert(midashi: String): Option[String] = entries get midashi map (_())
  def complete(midashi: String): Option[String] = None
}
