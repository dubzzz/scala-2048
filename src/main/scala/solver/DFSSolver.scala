package solver

import engine.{Down, GameManager, Grid, Left, Right, State2048, Up}

object DFSSolver {
  def scoreOf(game: GameManager[State2048]): Int =
    Grid.toStreams(game.state.grid).flatten.max

  def solve(game: GameManager[State2048], target: Int): Option[GameManager[State2048]] = {
    if (scoreOf(game) >= target) {
      printf("You've reached the target score, current is: %d\n", scoreOf(game))
      Some(game)
    }
    else (Left :: Down :: Right :: Up :: List.empty).toStream
        .map(dir => game.next(dir).flatMap(solve(_, target)))
        .filter(g => ! g.isEmpty)
        .map(g => g.get)
        .headOption
  }
}
