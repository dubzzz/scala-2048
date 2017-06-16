package engine

import utility.random.RandomGenerator

class GameManager(val state: GameState) {
  def next(dir: Direction): Option[GameManager] = {
    state.next(dir).map(new GameManager(_))
  }
  def newGame(): GameManager = {
    GameManager.of(state.rng)
  }
}

object GameManager {
  def of(rng: RandomGenerator[Int]): GameManager = {
    new GameManager(GameState.newGame(rng))
  }
}
