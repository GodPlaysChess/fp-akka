package iozio

object measure {
  def measure[A](f: => A): A = {
    val t1  = System.currentTimeMillis
    val res = f
    println(s"Took ${System.currentTimeMillis - t1} ms to compute $res")
    res
  }
}