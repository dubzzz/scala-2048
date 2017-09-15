package env.console

import engine.{Down, GameManager, Grid, Left, Right, State2048, Up}
import solver.DFSSolver
import utility.random.MersenneTwister

object Console {
  def main(args: Array[String]): Unit = {
    var quitGame = false
    var seed = System.currentTimeMillis.toInt
    var numNewGames = 0
    var game = GameManager.of(MersenneTwister.of(seed), State2048.newGame(_, 4))
    while (! quitGame) {
      printf("Current state (%d points): \n\n", game.state.score)
      Grid.toStreams(game.state.grid).foreach(line_stream => {
        print('\t')
        line_stream.foreach(printf("%d\t", _))
        print('\n')
      })

      print('\n')
      val choice = scala.io.StdIn.readLine("Your choice (left/right/up/down/undo/redo/new/solve/state/quit): ")
      choice match {
        case "left"  => game = game.next(Left).getOrElse(game)
        case "right" => game = game.next(Right).getOrElse(game)
        case "up"    => game = game.next(Up).getOrElse(game)
        case "down"  => game = game.next(Down).getOrElse(game)
        case "undo"  => game = game.undo().getOrElse(game)
        case "redo"  => game = game.redo().getOrElse(game)
        case "new"   => {
          numNewGames += 1
          game = game.newGame()
        }
        case "solve" => {
          try {
            val target = scala.io.StdIn.readLine("Expected score: ").toInt
            game = DFSSolver.solve(game, target).getOrElse(game)
          }
          catch { case e: Exception => printf("[ERROR] Unexpected exception: %s\n", e) }
        }
        case "state" => printf(s"#seed=${seed}&id=${numNewGames}&history=${game.stringify()}\n")
        case "quit"  => quitGame = true
        case _       => printf("[ERROR] Unknown choice: %s\n", choice)
      }
    }
  }
}
