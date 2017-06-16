package env.console

import engine.{Down, GameManager, Grid, Left, Right, Up}
import utility.random.MersenneTwister

object Console {
  def main(args: Array[String]): Unit = {
    var quitGame = false
    var game = GameManager.of(MersenneTwister.of(System.currentTimeMillis.toInt))
    while (! quitGame) {
      printf("Current state (%d points): \n\n", game.state().score)
      Grid.toStreams(game.state().grid).foreach(line_stream => {
        print('\t')
        line_stream.foreach(printf("%d\t", _))
        print('\n')
      })

      print('\n')
      val choice = scala.io.StdIn.readLine("Your choice (left/right/up/down/undo/new/quit): ")
      choice match {
        case "left"  => game = game.next(Left).getOrElse(game)
        case "right" => game = game.next(Right).getOrElse(game)
        case "up"    => game = game.next(Up).getOrElse(game)
        case "down"  => game = game.next(Down).getOrElse(game)
        case "undo"  => game = game.undo().getOrElse(game)
        case "new"   => game = game.newGame()
        case "quit"  => quitGame = true
        case _       => printf("[ERROR] Unknown choice: %s\n", choice)
      }
    }
  }
}
