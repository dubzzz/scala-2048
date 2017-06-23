package utility.random

import scala.annotation.tailrec

object UniformDistribution {
  
  type RNG = RandomGenerator[Int]
  val MIN_VALUE: Long = -(1L << 31)
  val MAX_VALUE: Long = (1L << 31) -1
  val NUM_VALUES: Long = MAX_VALUE - MIN_VALUE +1
  
  type Generator[A] = RNG => (A, RNG)

  def arbitrary: Generator[Int] = (rng: RNG) => rng.next

  def inRange(from: Int, to: Int): Generator[Int] = {
    val diff: Long = to.asInstanceOf[Long] - from.asInstanceOf[Long] +1
    val max_allowed: Long = NUM_VALUES - (NUM_VALUES % diff)
    
    @tailrec def go(r: RNG): (Int, RNG) = {
      val (v, nrng) = r.next
      val vv = v.asInstanceOf[Long] - MIN_VALUE
      if (vv < max_allowed) ((vv % diff + from.asInstanceOf[Long]).asInstanceOf[Int], nrng)
      else go(nrng)
    }

    (rng: RNG) => go(rng)
  }

  def ofBool: Generator[Boolean] = 
    (rng: RNG) => inRange(0, 1)(rng) match {
      case (0, nrng) => (false, nrng)
      case (_, nrng) => ( true, nrng)
    }

  def of[A](first: A, others: A*): Generator[A] = 
    (rng: RNG) => inRange(0, others.size)(rng) match {
      case (0, nrng) => (first, nrng)
      case (n, nrng) => (others(n-1), nrng)
    }

  def frequencyOption[A](choices: (Int, A)*): Option[Generator[A]] = {
    val s = choices.toStream
    val count = s.map(_._1).fold(0)(_ + _)
    
    @tailrec def get(ss: Stream[(Int, A)], idx: Int): A = //ss should never be empty
      if (idx < ss.head._1) ss.head._2
      else get(ss.tail, idx - ss.head._1)

    if (count == 0 || s.map(_._1).exists(_ < 0)) None
    else Some(
      (rng: RNG) => inRange(0, count -1)(rng) match {
        case (n, nrng) => (get(s, n), nrng) })
  }
  def frequency[A](choices: (Int, A)*): Generator[A] = frequencyOption(choices: _*).get
}
