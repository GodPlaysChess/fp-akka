package iozio

import scala.concurrent.Future

object measure {
  def measure[A](f: => A): A = {
    val t1  = System.currentTimeMillis
    val res = f
    println(s"Took ${System.currentTimeMillis - t1} ms to compute $res")
    res
  }
}

object vla extends App {
  import scala.concurrent.ExecutionContext.Implicits.global
  import scalaz._
  import Scalaz._
  // L[T[F[A]]] => F[L[T[A]]
  implicit def T[A] = Traverse[List].compose[(A, ?)]

  def list[A, X]: List[(A, Future[X])] => Future[List[(A, X)]] = ltfa => {
    val v1 = ltfa.traverse(_.sequence)
    T[A].sequence(ltfa)
  }

  println(NonEmptyList(1, 2, 3).duplicate)
}
