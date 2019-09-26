package datastructures

object CoJoint {}
// Either with Isomorphism
sealed trait CoJoint[A, B] {
  def to: A => B
  def from: B => A
}

trait <[A] extends CoJoint[A, Nothing]
trait >[B] extends CoJoint[Nothing, B]

// Example
sealed trait Number {
  // naive
  def +(number: Number): Number = (this, number) match {
    case (PositiveInt(i1), PositiveInt(i2)) => PositiveInt(i1 + i2)
    case (NegativeInt(i1), NegativeInt(i2)) => NegativeInt(i1 + i2)
    case (PositiveInt(i1), NegativeInt(i2)) =>
      if (i1 >= i2) PositiveInt(i1 - i2)
      else NegativeInt(i2 - i1)
    case _ => number + this
  }
}
case class PositiveInt(i: Int) extends Number
case class NegativeInt(i: Int) extends Number


object test extends App {
  val me = PositiveInt(5)
  println(s"Hello ${s"$me"}")
}
