package console

import engine.{Down, GameState, Grid, Left, Right, Up}
import utility.random.MersenneTwister

object Console {
  def newGame(): GameState =
    GameState.newGame(MersenneTwister.of(System.currentTimeMillis.toInt))

  def main(args: Array[String]): Unit = {
    var quitGame = false
    var state = newGame()
    while (! quitGame) {
      printf("Current state (%d points): \n\n", state.score)
      Grid.toStreams(state.grid).foreach(line_stream => {
        print('\t')
        line_stream.foreach(printf("%d\t", _))
        print('\n')
      })

      print('\n')
      val choice = scala.io.StdIn.readLine("Your choice (left/right/up/down/new/quit): ")
      choice match {
        case "left"  => state = state.next(Left).getOrElse(state)
        case "right" => state = state.next(Right).getOrElse(state)
        case "up"    => state = state.next(Up).getOrElse(state)
        case "down"  => state = state.next(Down).getOrElse(state)
        case "new"   => state = newGame()
        case "quit"  => quitGame = true
        case _       => printf("[ERROR] Unknown choice: %s\n", choice)
      }
    }
  }
}
