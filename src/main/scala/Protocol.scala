package skkserv

import java.awt.image.RescaleOp
import scala.util.matching.Regex

object Protocol {

  sealed trait Request
  object Request {
    final case object Close extends Request
    final case class Convert(midashi: String) extends Request
    final case object Version extends Request
    final case object Hostname extends Request
    final case class Abbreviate(midashi: String) extends Request
    final case class Invalid(text: String) extends Request
  }

//  sealed trait Response
}
