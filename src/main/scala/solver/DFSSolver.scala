package solver

import engine.{Direction, Down, GameManager, Grid, Left, Right, State2048, Up}

import scala.annotation.tailrec

object DFSSolver {
  trait IStrategy {
    def choices(game: GameManager[State2048]): List[Direction]
  }

  class FixedOrderStrategy extends IStrategy {
    def choices(game: GameManager[State2048]): List[Direction] =
      Left :: Down :: Right :: Up :: List.empty
  }

  def scoreOf(game: GameManager[State2048]): Int =
    Grid.toStreams(game.state.grid).flatten.max

  def enrich(choices: List[Direction]): List[Option[Direction]] =
    choices.flatMap(Some(_) :: None :: List.empty)

  @tailrec
  def solveHelper(game: GameManager[State2048], target: Int, strategy: IStrategy, steps: List[Option[Direction]]): Option[GameManager[State2048]] = {
    if (scoreOf(game) >= target)
      Some(game)
    else if (steps.isEmpty)
      None
    else if (steps.head.isEmpty)
      solveHelper(game.undo().get, target, strategy, steps.tail)
    else {
      val next = game.next(steps.head.get)
      if (next.isEmpty)
        solveHelper(game, target, strategy, steps.tail.tail)
      else
        solveHelper(next.get, target, strategy, enrich(strategy.choices(next.get)).foldRight(steps.tail)((d, l) => d :: l))
    }
  }

  def solve(game: GameManager[State2048], target: Int, strategy: IStrategy): Option[GameManager[State2048]] =
    solveHelper(game, target, strategy, enrich(strategy.choices(game)))
}
