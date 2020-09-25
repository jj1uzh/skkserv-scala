package skkserv

import scala.util.{Try, Using}
import scala.io.Source
import io.circe.parser.decode
import io.circe.generic.auto._

final case class Config(
    jisyoPlaces: Vector[String] = Vector.empty
)

object Config {
  def apply(path: String): Try[Config] =
    for {
      configStr <- Using(Source fromFile path)(_.getLines().mkString)
      config <- decode[Config](configStr).toTry
    } yield config
}
