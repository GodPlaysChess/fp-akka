package iozio.memoization

import java.util.concurrent.Executors

import iozio.measure.measure
import zio.DefaultRuntime

import scala.concurrent.{ Await, ExecutionContext, Future }

object io_example extends App {
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  import cats.effect.IO

  val f1 = IO { Thread.sleep(1000); 1 }

  measure(f1.unsafeRunSync())
  measure(f1.unsafeRunSync())
  // not possible https://github.com/typelevel/cats-effect/issues/120

}

object future_example extends App {
  import scala.concurrent.duration._
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  // memoisation by default
  val f1 = Future { Thread.sleep(500); 1 }
  measure(Await.result(f1, Duration.Inf))
  measure(Await.result(f1, Duration.Inf))
}

object zio_example extends App with DefaultRuntime {
  import zio._

  val f1 = IO { Thread.sleep(500); 1 }
  measure(unsafeRun(f1))
  measure(unsafeRun(f1))

  // one should operate on result
  // since everything is effect, then
  val memoized = for {
    f1memo <- f1.memoize
    i1     = measure(unsafeRun(f1memo)) // executing side effects inside is strongly discouraged
    i2     = measure(unsafeRun(f1memo)) // it's made for demonstration
  } yield i1 + i2

  measure(unsafeRun(memoized))
}
