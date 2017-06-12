package utility.random

/** MersenneTwister generator
  * Implemented in a pure fashion
  *
  * Inspired by https://en.wikipedia.org/wiki/Mersenne_Twister
  */
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

  def seeded(seed: Int): Stream[Int] = {
    def loop(prev: Int, id: Int): Stream[Int] =
      prev #:: loop(F * (prev ^ (prev >> 30)) + (id +1), id +1)
    loop(seed, 0).take(N)
  }

  def twisted(s: Stream[Int]): Stream[Int] = {
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
    loop(s, s.tail, s.drop(M)).take(N)
  }

  def extract(s: Stream[Int]): Stream[Int] = {
    s.map(y0 => {
      val y1 = y0 ^ (y0 >> U)
      val y2 = y1 ^ ((y1 << S) & B)
      val y3 = y2 ^ ((y2 << T) & C)
      y3 ^ (y3 >> L)
    })
  }

  def stream(seed: Int): Stream[Int] = {
    def go(prev: Stream[Int]): Stream[Int] = {
      val tw = twisted(prev)
      extract(tw).append(go(tw))
    }
    go(seeded(seed))
  }
}