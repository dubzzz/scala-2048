package utility.random

import scala.annotation.tailrec

object UniformDistribution {
  type RNG = RandomGenerator[Int]
  val MIN_VALUE: Long = -(1L << 31)
  val MAX_VALUE: Long = (1L << 31) -1
  val NUM_VALUES: Long = MAX_VALUE - MIN_VALUE +1

  def nextInt(rng: RNG): (Int, RNG) = rng.next

  def nextInt(from: Int, to: Int)(rng: RNG): (Int, RNG) = {
    val diff: Long = to.asInstanceOf[Long] - from.asInstanceOf[Long] +1
    val max_allowed: Long = NUM_VALUES - (NUM_VALUES % diff)

    @tailrec def go(r: RNG): (Int, RNG) = {
      val (v, nrng) = r.next
      val vv = v.asInstanceOf[Long] - MIN_VALUE
      if (vv < max_allowed) ((vv % diff + from.asInstanceOf[Long]).asInstanceOf[Int], nrng)
      else go(nrng)
    }
    go(rng)
  }

  def nextBoolean(rng: RNG): (Boolean, RNG) = {
    val (v, nrng) = nextInt(0, 1)(rng)
    (v == 1, nrng)
  }

  def nextOf[A](first: A, others: A*)(rng: RNG): (A, RNG) = {
    val (v, nrng) = nextInt(0, others.size)(rng)
    if (v == 0) (first, nrng) else (others(v-1), nrng)
  }

  def nextFrequency[A](choices: (Int, A)*)(rng: RNG): (A, RNG) = {
    val s = choices.toStream
    val count = s.map(_._1).fold(0)(_ + _)
    val (v, nrng) = nextInt(0, count -1)(rng)
    
    @tailrec def get(ss: Stream[(Int, A)], idx: Int): A = //ss should never be empty
      if (idx < ss.head._1) ss.head._2
      else get(ss.tail, idx - ss.head._1)

    (get(s, v), nrng)
  }
}
