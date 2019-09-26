package simulation

object LambdaExpansions extends App {

  val x: Int => Int = TryThis("function notation").ok
  x(1)
  x(1)
  x(1)

  val y: Int => Int = i => TryThis("undescore notation").ok(i)

  y(1)
  y(1)
  y(1)
}

object TryThis {
  def apply(from: String): TryThis = {
    println(s"Apply is called by $from")
    new TryThis {}
  }

}

trait TryThis {
  def ok(i: Int): Int = 1
}

