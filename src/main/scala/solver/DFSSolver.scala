package solver

import engine.{Direction, Down, GameManager, Grid, Left, Right, State2048, Up}

import scala.annotation.tailrec

object DFSSolver {
  trait IStrategy {
    def choices(game: GameManager[State2048]): List[Direction]
  }
  trait IScore {
    def score(game: GameManager[State2048]): Int
  }

  class FixedOrderStrategy extends IStrategy {
    def choices(game: GameManager[State2048]): List[Direction] =
      Left :: Down :: Right :: Up :: List.empty
  }
  class CircularOrderStrategy extends IStrategy {
    def choices(game: GameManager[State2048]): List[Direction] = {
      val xs: List[Direction] = Left :: Down :: Right :: Up :: Left :: Down :: Right :: Up :: List.empty
      xs.drop(game.prevHistory.size % 4).take(4).toList
    }
  }
  class NextStepStrategy(val scoring: IScore) extends IStrategy {
    def choices(game: GameManager[State2048]): List[Direction] = {
      val xs: List[Direction] = Left :: Down :: Right :: Up :: List.empty
      xs.map(direction => (direction, game.next(direction)))
        .filter(! _._2.isEmpty)
        .map(data => (data._1, scoring.score(data._2.get)))
        .sortWith((a, b) => a._2 > b._2)
        .map(_._1)
    }
  }
  class MaxHolesScore extends IScore {
    def score(game: GameManager[State2048]): Int =
      Grid.toStreams(game.state.grid).flatten.count(_ == 0)
  }
  class SnakeScore extends IScore {
    def score(game: GameManager[State2048]): Int = {
      val size = game.state.grid.length
      val ws = Array.tabulate(size, size)((x: Int, y: Int) =>
        if (y % 2 == 0) y*size + x+1
        else y*size + size-x)
      Grid.toStreams(game.state.grid).flatten
        .map(Math.max(_, 1))
        .map(v => Math.round(Math.log(v)/Math.log(2)))
        .zip(Grid.toStreams(ws).flatten)
        .map(i => i._1 * i._2)
        .sum
        .toInt
    }
  }
  class BorderScore extends IScore {
    def score(game: GameManager[State2048]): Int = {
      val size = game.state.grid.length
      val ws = Array.tabulate(size, size)((x: Int, y: Int) =>
        if (y == 0 || y == size -1 || x == 0 || x == size -1) 1
        else 0)
      Grid.toStreams(game.state.grid).flatten
        .zip(Grid.toStreams(ws).flatten)
        .map(i => i._1 * i._2)
        .sum
    }
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
