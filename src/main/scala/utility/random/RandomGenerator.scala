package utility.random

abstract class RandomGenerator[A] {
  def next(): (A, RandomGenerator[A]);
}
