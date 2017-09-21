package mvc

import engine.{GameManager, Left, Direction, State2048}
import utility.random.MersenneTwister

object BoardController {
  type Model = GameManager[State2048]

  def init(numTiles: Int): Model = GameManager.of(MersenneTwister.of(_), State2048.newGame(_, numTiles), System.currentTimeMillis.toInt)
  def load(numTiles: Int, locationHash: String): Model = GameManager.of(MersenneTwister.of(_), State2048.newGame(_, numTiles), locationHash.substring(1))

  def newGame(game: Model): Option[Model] = Some(game.newGame())
  def play(game: Model, key: Direction): Option[Model] = game.next(key)
  def undo(game: Model): Option[Model] = game.undo()
  def redo(game: Model): Option[Model] = game.redo()
}
