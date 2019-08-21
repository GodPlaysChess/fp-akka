package iozio.error_handling

import java.util.concurrent.Executors

import cats.data.EitherT
import iozio.measure.measure
import zio.DefaultRuntime

import scala.concurrent.{ Await, ExecutionContext, Future }

object io_example extends App {

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  import cats.effect.IO

  def f1 = IO { throw new RuntimeException("Database error"); 1 }
  def f2 = IO {  1 }

  // can only bimap since it's not parametric over `E`
  measure(f1.handleErrorWith(_ => f1).unsafeRunSync())
  measure(f1.redeem(_ => 0, identity).unsafeRunSync())
  measure(f1.redeemWith(_ => f1, _ => f1).unsafeRunSync())

  import cats.implicits._

  // in oder to gain granularity and power cats resides on MTL
  def domainCall: EitherT[IO, ErrorAlgebra, Int] =
    f2.attemptT.leftMap(_ => FailWhale)

  val program = for {
    x1 <- domainCall
    x2 <- domainCall
  } yield x1 + x2

  measure(program.value.unsafeRunSync())

  // it's fine for now, but it explode in complexity when you try to put more on top
  // such as parallelism, another monad
  // adds memory overhead.
  // in haskell though it's all ok, but we leave that for  another talk

}

object future_example extends App {
  import scala.concurrent.duration._
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  val f1 = Future { throw new RuntimeException("Database error"); 1 }
  measure(Await.result(f1.failed, Duration.Inf))

  // there are 2 common ways to handle errors is via `recover` and `transform`. For example you can't simply map error to your domain.
  // can only bimap since it's not parametric over `E`
  measure(Await.result(f1.transform(identity, _ => FailWhale), Duration.Inf))
  measure(Await.result(f1.recover { case _ => 2 }, Duration.Inf))

  // one of the common practices - to agree that Future never fails, and use make a result an Either.
  // it gives you transparency about erroes + fine grained control over it on the type level as well.

  // such as type-class utilisation, error aggregation etc.. // example here!

  def mightFail: Future[Either[Throwable, Int]] = ???

}

object zio_example extends App with DefaultRuntime {
  import zio._

  val f1: Task[Int] = IO { throw new RuntimeException("Database error"); 1 }
  val f2: IO[ErrorAlgebra, Int] = f1.mapError(_ => FailWhale)
  measure(unsafeRun(f2))
  measure(unsafeRun(f1))
  // that's it. Complexity is just another type which can be hidden byt he type alias.
  // superior type inference
}

sealed trait ErrorAlgebra
object FailWhale extends Throwable with ErrorAlgebra
