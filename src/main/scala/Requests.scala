package skkserv

import scala.io.Source
import skkserv.Protocol.Request
import skkserv.Protocol.Request._

case class Requests(source: Source) extends Iterator[Request] {

  def hasNext: Boolean = true

  def next(): Request = {
    def nextUntilWhitespace: String = {
      source.next match {
        case ' ' => ""
        case ch  => ch +: nextUntilWhitespace
      }
    }

    source.next match {
      case '0'     => Close
      case '1'     => Convert(nextUntilWhitespace)
      case '2'     => Version
      case '3'     => Hostname
      case '4'     => Abbreviate(nextUntilWhitespace)
      case default => Invalid(default.toString)
    }
  }
}
