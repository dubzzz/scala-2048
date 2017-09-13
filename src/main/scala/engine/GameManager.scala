package engine

import utility.random.RandomGenerator

import scala.annotation.tailrec

class GameManager[State <: GameState](val builder: RandomGenerator[Int] => State, val prevHistory: List[(State, Direction)], val nextMoves: List[Direction]) {
  def initialState = {
    @tailrec def go(l: List[State], s: State): State = l match {
      case Nil    => s
      case t :: q => go(q, t)
    }
    go(prevStates.tail, prevStates.head)
  }
  def prevStates = prevHistory.map(_._1)
  def state = prevStates.head

  def next(dir: Direction): Option[GameManager[State]] = {
    state.next(dir).map(nextS => new GameManager(builder, (nextS.asInstanceOf[State], dir) :: prevHistory, Nil))
  }
  def undo(): Option[GameManager[State]] = {
    if (! prevStates.tail.isEmpty) Some(new GameManager(builder, prevHistory.tail, prevHistory.head._2 :: nextMoves))
    else None
  }
  def redo(): Option[GameManager[State]] = {
    if (! nextMoves.isEmpty) state.next(nextMoves.head).map(nextS => new GameManager(builder, (nextS.asInstanceOf[State], nextMoves.head) :: prevHistory, nextMoves.tail))
    else None
  }
  def undoAll(): GameManager[State] = undo() match {
    case None    => this
    case Some(g) => g.undoAll()
  }
  def redoAll(): GameManager[State] = redo() match {
    case None    => this
    case Some(g) => g.redoAll()
  }
  def newGame(): GameManager[State] = {
    GameManager.of(initialState.rng.next()._2, builder)
  }

  def stringify(): String = {
    prevHistory.map(_._2).map(_ match {
      case Left  => 'L'
      case Right => 'R'
      case Down  => 'D'
      case Up    => 'U'
    }).foldLeft("")((s, c) => c + s)
      .substring(1)
  }
  def parse(in: String): GameManager[State] = {
    new GameManager(builder,
      (initialState, Left) :: Nil,
      in.map(_ match {
        case 'L' => Left
        case 'R' => Right
        case 'U' => Up
        case  _  => Down
      }).to[List]
    )
  }
}

object GameManager {
  def of[State <: GameState](rng: RandomGenerator[Int], builder: RandomGenerator[Int] => State): GameManager[State] = {
    new GameManager(builder, (builder(rng), Left) :: Nil, Nil)
  }
}
