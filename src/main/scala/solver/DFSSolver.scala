package solver

import engine.{Direction, Down, GameManager, Grid, Left, Right, State2048, Up}

import scala.annotation.tailrec

object DFSSolver {
  def scoreOf(game: GameManager[State2048]): Int =
    Grid.toStreams(game.state.grid).flatten.max

  @tailrec
  def solveHelper(game: GameManager[State2048], target: Int, steps: List[Option[Direction]]): Option[GameManager[State2048]] = {
    if (scoreOf(game) >= target)
      Some(game)
    else if (steps.isEmpty)
      None
    else if (steps.head.isEmpty)
      solveHelper(game.undo().get, target, steps.tail)
    else {
      val next = game.next(steps.head.get)
      if (next.isEmpty)
        solveHelper(game, target, steps.tail.tail)
      else
        solveHelper(next.get, target, Some(Left) :: None :: Some(Down) :: None :: Some(Right) :: None :: Some(Up) :: None :: steps.tail)
    }
  }

  def solve(game: GameManager[State2048], target: Int): Option[GameManager[State2048]] =
    solveHelper(game, target, Some(Left) :: None :: Some(Down) :: None :: Some(Right) :: None :: Some(Up) :: None :: List.empty)
}
