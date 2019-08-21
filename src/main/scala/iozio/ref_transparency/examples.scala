package iozio.ref_transparency

import java.util.concurrent.Executors

import iozio.measure.measure
import java.util.concurrent.Executors

import cats.{ effect, Applicative, Parallel }
import zio.DefaultRuntime

import scala.concurrent.{ Await, ExecutionContext, ExecutionContextExecutor, Future }

object io_example extends App {
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  import cats.effect.IO

  // ref transparency:
  val f1 = IO { Thread.sleep(500); 1 }
  val f2 = IO { Thread.sleep(500); 2 }

  def fr1 =
    for {
      r1 <- f1
      r2 <- f2
    } yield r1 + r2

  def fr2 =
    for {
      r1 <- IO { Thread.sleep(500); 1 }
      r2 <- IO { Thread.sleep(500); 2 }
    } yield r1 + r2

  measure(fr1.unsafeRunSync())
  measure(fr2.unsafeRunSync())

  // sequential
  import cats.implicits._
  import cats.syntax.parallel._

  measure((f1 map2 f2)(_ * _).unsafeRunSync())

  //parallel
  implicit val A: Applicative[effect.IO.Par]    = IO.parApplicative(cats.effect.IO.contextShift(ec))
  val par2: _root_.cats.effect.IO.Par.Type[Int] = (IO.Par(f1) map2 IO.Par(f2))(_ * _)
  (List(1, 2, 3), List(4, 5, 6)).mapN(_ + _)
  measure(par2.asInstanceOf[IO[Int]].unsafeRunSync())

}

object future_example extends App {
  import scala.concurrent.duration._
  implicit val ec = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  //Absence of referential transparency
  val f1 = Future { Thread.sleep(500); 1 }
  val f2 = Future { Thread.sleep(500); 2 }

  def fr1 =
    for {
      r1 <- f1
      r2 <- f2
    } yield r1 + r2

  def fr2 =
    for {
      r1 <- Future { Thread.sleep(500); 1 }
      r2 <- Future { Thread.sleep(500); 2 }
    } yield r1 + r2

  measure(Await.result(fr1, Duration.Inf))
  measure(Await.result(fr2, Duration.Inf))
  measure(Await.result(Future { Thread.sleep(500); 1 }.zip(Future { Thread.sleep(500); 1 }), Duration.Inf))
}

object zio_example extends App with DefaultRuntime {

  import zio._

  // ref transparency:
  val f1 = IO { Thread.sleep(500); 1 }
  val f2 = IO { Thread.sleep(500); 2 }

  def fr1 =
    for {
      r1 <- f1
      r2 <- f2
    } yield r1 + r2

  def fr2 =
    for {
      r1 <- IO { Thread.sleep(500); 1 }
      r2 <- IO { Thread.sleep(500); 2 }
    } yield r1 + r2

  measure(unsafeRun(fr1))
  measure(unsafeRun(fr2))

  // sequential
  measure(unsafeRun(f1 <*> f2))
  // parallel
  measure(unsafeRun(f1 zipPar f2))
}
