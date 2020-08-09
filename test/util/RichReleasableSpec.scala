package skkserv.util

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers._
import org.scalatest.TryValues._
import scala.util.Using.Releasable
import scala.util.{Success, Failure}
import scala.io.Source

class RichReleasableSpec extends AnyWordSpec {

  import skkserv.util.RichReleasable.ReleasableForComprehension

  case class Resource(n: Int) {
    var closed = false
    def throwError(): Unit = throw new Exception("error")
  }

  object Resource {
    def apply(): Resource = throw new Exception("error")

    implicit object ResourceIsReleasable extends Releasable[Resource] {
      def release(resource: Resource): Unit = resource.closed = true
    }
  }

  "ReleasableForComprehension" should {
    "mapで正しくリソースを解放する" in {
      val r = Resource(1)
      val inBlock = for { r <- r } yield { r.closed }

      inBlock mustBe Success(false)
      r.closed mustBe true
    }

    "リソースのnewもFailureにする" in {
      val result = for { r <- Resource() } yield { () }

      result mustBe a[Failure[_]]
    }

    "flatMapがちゃんと動く" in {
      val result0 =
        for {
          r1 <- Resource(5)
          r2 <- Resource(6)
        } yield r1.n * r2.n

      lazy val resource1 = Resource(2)
      val result1 =
        for {
          r1 <- resource1
          r2 <- Resource()
        } yield r1.n * r2.n

      lazy val resource2 = Resource(3)
      val result2 =
        for {
          r1 <- Resource(1)
          r2 <- resource2
        } yield r1.throwError()

      result0 mustBe Success(30)
      resource1.closed mustBe true
      result1 mustBe a[Failure[_]]
      resource2.closed mustBe true
      result2 mustBe a[Failure[_]]
    }
  }
}
