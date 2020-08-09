package skkserv.util

import scala.util.{Try, Failure}
import scala.util.Using.Releasable
import scala.util.control.Exception.{allCatch, ultimately}

object RichReleasable {

  implicit class ReleasableForComprehension[T](resource: => T)(implicit releaser: Releasable[T]) {

    def map[U](f: T => U): Try[U] =
      for {
        resource <- allCatch withTry resource
        result   <- allCatch andFinally { releaser release resource } withTry f(resource)
      } yield result

    def flatMap[U](f: T => Try[U]): Try[U] =
      for {
        resource <- allCatch withTry resource
        result   <- allCatch andFinally { releaser release resource } withApply { e => Failure(e) } apply f(resource)
      } yield result

    def foreach(p: T => Unit) = map(p)
  }
}
