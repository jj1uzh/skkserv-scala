package skkserv

import scala.util.{Try, Using}
import scala.io.Source
import io.circe.Error
import io.circe.parser.decode
import io.circe.generic.auto._

final case class Config(
    jisyoPlaces: List[String] = Nil
)

object Config {
  def apply(path: String): Try[Config] =
    for {
      configStr <- Using(Source fromFile path)(_.getLines().mkString)
      config <- decode[Config](configStr).toTry
    } yield config
}
