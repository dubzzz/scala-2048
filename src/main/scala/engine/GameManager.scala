package engine

import utility.random.RandomGenerator

class GameManager(val previous_states: List[GameState]) {
  def state(): GameState = {
    previous_states.head
  }
  def next(dir: Direction): Option[GameManager] = {
    state().next(dir).map(next_s => new GameManager(next_s :: previous_states))
  }
  def undo(): Option[GameManager] = {
    if (! previous_states.tail.isEmpty) Some(new GameManager(previous_states.tail))
    else None
  }
  def newGame(): GameManager = {
    GameManager.of(previous_states.head.rng)
  }
}

object GameManager {
  def of(rng: RandomGenerator[Int]): GameManager = {
    new GameManager(GameState.newGame(rng) :: Nil)
  }
}
