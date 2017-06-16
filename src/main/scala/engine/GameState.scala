package engine

import utility.random.RandomGenerator

abstract class GameState(val rng: RandomGenerator[Int]) {
  def next(direction: Direction): Option[GameState];
}
