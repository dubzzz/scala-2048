package utility.random

trait RandomGenerator[A] {
  def next(): (A, RandomGenerator[A]);
}
