package iozio.trampolining

import java.util.concurrent.Executors

import iozio.measure.measure
import iozio.trampolining.zio_example.unsafeRun
import zio.DefaultRuntime

import scala.concurrent.{Await, ExecutionContext, Future}

object io_example extends App {
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  import cats.effect.IO

  val f1 = (1 to 1000000).foldLeft(IO.pure(0))((a, _) => a.map(_ + 1))

  measure(f1.unsafeRunSync())
}

object future_example extends App {
  import scala.concurrent.duration._
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  // Bouncing to different execution context is cumbersome and degrades performance
  def f1 = (1 to 1000000).foldLeft(Future.successful(0))((a, _) => a.map(_ + 1))
  measure(Await.result(f1, Duration.Inf))
}

object future_example_recursion extends App {
  import scala.concurrent.duration._
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  def blowUp(n: Int): Future[Int] = for {
    x <- if (n == 0) Future.successful(0) else blowUp(n - 1)
  } yield x + 1

  measure(Await.result(blowUp(10000), Duration.Inf))
}


object zio_example extends App with DefaultRuntime {
  import zio._

  val f1 = (1 to 1000000).foldLeft(IO.succeed(0))((a, _) => a.map(_ + 1))
  measure(unsafeRun(f1))

  // using Arrows it's even faster, bu subject for another talk
//  val f2: FunctionIO[Nothing, Int, Int] = (1 to 1000).foldLeft(FunctionIO.identity[Int])((a, _) => a.map(_ + 1))
//  measure(unsafeRun(f2.run(0)))
}

object zio_example_recursion extends App {
  import zio._

  def blowUp(n: Int): IO[Throwable, Int] = for {
    x <- if (n == 0) IO.succeed(0) else blowUp(n - 1)
  } yield x + 1

  measure(unsafeRun(blowUp(10000)))
}





















object Matthias  extends App {
  trait Foo { def get[A]: List[A] }
  def foo = new Foo { override def get[A]: List[A] = List.empty[A] }
  def first(list: Foo): Int = { list.get[Int].size }
  measure(first(foo))

  trait Foo1[A] { def get: List[A] }
  def foo1[A] = new Foo1[A] { override def get: List[A] = List.empty[A] }
  def first1(list: Foo1[A forSome {type A}]): Int = { list.get.size } // no type on `get` here
  measure(first1(foo1))
 }
