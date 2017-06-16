package engine

import utility.random.RandomGenerator

class GameManager[State <: GameState](val builder: RandomGenerator[Int] => State, val prevStates: List[State], val redoStates: List[State]) {
  def state = prevStates.head

  def next(dir: Direction): Option[GameManager[State]] = {
    state.next(dir).map(nextS => new GameManager(builder, nextS.asInstanceOf[State] :: prevStates, Nil))
  }
  def undo(): Option[GameManager[State]] = {
    if (! prevStates.tail.isEmpty) Some(new GameManager(builder, prevStates.tail, prevStates.head :: redoStates))
    else None
  }
  def redo(): Option[GameManager[State]] = {
    if (! redoStates.isEmpty) Some(new GameManager(builder, redoStates.head :: prevStates, redoStates.tail))
    else None
  }
  def newGame(): GameManager[State] = {
    GameManager.of(prevStates.head.rng, builder)
  }
}

object GameManager {
  def of[State <: GameState](rng: RandomGenerator[Int], builder: RandomGenerator[Int] => State): GameManager[State] = {
    new GameManager(builder, builder(rng) :: Nil, Nil)
  }
}
