package utility.random

/** MersenneTwister generator
  * Implemented in a pure fashion
  *
  * Inspired by https://en.wikipedia.org/wiki/Mersenne_Twister
  */
class MersenneTwister(val stream: Stream[Int]) extends RandomGenerator[Int] {
  override def next(): (Int, MersenneTwister) =
    (stream.head, new MersenneTwister(stream.tail))
}

object MersenneTwister {
  val N = 624
  val M = 397
  val R = 31
  val A = 0x9908B0DF
  val F = 1812433253
  val U = 11
  val S = 7
  val B = 0x9D2C5680
  val T = 15
  val C = 0xEFC60000
  val L = 18
  val MASK_LOWER = (1 << R) - 1
  val MASK_UPPER = (1 << R)

  def of(seed: Int): MersenneTwister =
    new MersenneTwister(MersenneTwister.stream(seed))

  def stream(seed: Int): Stream[Int] = {
    def seeded(prev: Int, id: Int): Stream[Int] = {
      prev #:: seeded(F * (prev ^ (prev >> 30)) + (id + 1), id + 1)
    }
    def twisted_next(cur: Int, next: Int, nextM: Int): Int = {
      val x = (cur & MASK_UPPER) + (next & MASK_LOWER)
      val xA = if ((x & 1) == 1) ((x >> 1) ^ A) else (x >> 1)
      nextM ^ xA
    }
    def loop(cur: Stream[Int], next: Stream[Int], nextM: Stream[Int]): Stream[Int] = {
      val nextValues = (cur, next, nextM).zipped.map(twisted_next(_,_,_))
      nextValues.append(
        loop(
          cur.append(nextValues).drop(N-M),
          next.append(nextValues).drop(N-M),
          nextValues))
    }

    val s = seeded(seed, 0).take(N)
    loop(s, s.tail, s.drop(M)).map(y0 => {
      val y1 = y0 ^ (y0 >> U)
      val y2 = y1 ^ ((y1 << S) & B)
      val y3 = y2 ^ ((y2 << T) & C)
      y3 ^ (y3 >> L)
    })
  }
}