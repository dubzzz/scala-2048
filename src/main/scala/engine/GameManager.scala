package engine

import utility.random.RandomGenerator

class GameManager(val prevStates: List[GameState], val redoStates: List[GameState]) {
  def state(): GameState = {
    prevStates.head
  }
  def next(dir: Direction): Option[GameManager] = {
    state().next(dir).map(nextS => new GameManager(nextS :: prevStates, Nil))
  }
  def undo(): Option[GameManager] = {
    if (! prevStates.tail.isEmpty) Some(new GameManager(prevStates.tail, prevStates.head :: redoStates))
    else None
  }
  def redo(): Option[GameManager] = {
    if (! redoStates.isEmpty) Some(new GameManager(redoStates.head :: prevStates, redoStates.tail))
    else None
  }
  def newGame(): GameManager = {
    GameManager.of(prevStates.head.rng)
  }
}

object GameManager {
  def of(rng: RandomGenerator[Int]): GameManager = {
    new GameManager(GameState.newGame(rng) :: Nil, Nil)
  }
}
