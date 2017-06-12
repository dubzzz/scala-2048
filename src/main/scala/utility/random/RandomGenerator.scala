package utility.random

abstract class RandomGenerator[A] {
  val MIN_VALUE: Long = -(1L << 31)
  val MAX_VALUE: Long = (1L << 31) -1
  val NUM_VALUES: Long = 1L << 32
  def next(): (A, RandomGenerator[A]);
}
