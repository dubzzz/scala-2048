package engine

import org.junit.Test
import org.scalacheck.{Gen, Prop}
import org.scalatest.junit.JUnitSuite
import org.scalatest.prop.Checkers
import utility.random.RandomGenerator

class GameManagerTest extends JUnitSuite with Checkers {
  class FakeRandom(v: Int) extends RandomGenerator[Int] {
    override def next(): (Int, RandomGenerator[Int]) = (v+1, new FakeRandom(v+1))
  }
  class FakeState(rng: RandomGenerator[Int], val v: (Direction, Int) = (Left, -1)) extends GameState(rng) {
    override def next(dir: Direction): Option[FakeState] = {
      val (v, nrng) = rng.next()
      Some(new FakeState(nrng, (dir, v)))
    }
  }

  val genGameCommands = for {
    directions <- Gen.listOf(Gen.oneOf(Left, Right, Up, Down))
  } yield (directions)
  val genGameCommandsAndUndo = for {
    directions <- Gen.listOf(Gen.oneOf(Left, Right, Up, Down)) suchThat (! _.isEmpty)
    numUndos   <- Gen.choose(0, directions.size)
  } yield (directions, numUndos)

  @Test
  def propertyNewGameDifferentRndFromPreviousGame(): Unit = {
    check(Prop.forAll(genGameCommands) { directions =>
      val gInit = GameManager.of(new FakeRandom(0), new FakeState(_))
      val gMoves = directions.foldLeft(gInit) ((g, dir) => g.next(dir).getOrElse(g))
      gInit.state.rng.next()._1 != gMoves.newGame().state.rng.next()._1
    })
  }

  @Test
  def propertyWhateverTheStartingPointNewGameIsTheSame(): Unit = {
    check(Prop.forAll(genGameCommands) { directions =>
      val g1 = GameManager.of(new FakeRandom(0), new FakeState(_))
      val g2 = directions.foldLeft(GameManager.of(new FakeRandom(0), new FakeState(_))) ((g, dir) => g.next(dir).getOrElse(g))
      g1.newGame().state.rng.next()._1 == g2.newGame().state.rng.next()._1
    })
  }

  @Test
  def propertyValidNextKickAllPossibleRedo(): Unit = {
    check(Prop.forAll(genGameCommandsAndUndo) { data =>
      data match {
        case (directions, numUndos) =>
          val moves = directions.tail
          val afterMove = directions.head

          val gInit = GameManager.of(new FakeRandom(0), new FakeState(_))
          val gMoves = moves.foldLeft(gInit)((g, dir) => g.next(dir).getOrElse(g))
          val gMovesUndo = Stream.continually(0).take(numUndos).foldLeft(gMoves)((g, _) => g.undo().getOrElse(g))
          gMovesUndo.next(afterMove).get.redo() == None
      }
    })
  }

  @Test
  def propertyUndoRevertCommands(): Unit = {
    check(Prop.forAll(genGameCommandsAndUndo) { data =>
      data match {
        case (directions, numUndos) =>
          val gInit = GameManager.of(new FakeRandom(0), new FakeState(_))
          val gMoves = directions.foldLeft(gInit)((g, dir) => g.next(dir).getOrElse(g))
          val gMovesUndo = Stream.continually(0).take(numUndos).foldLeft(gMoves)((g, _) => g.undo().getOrElse(g))

          gMovesUndo.prevStates.size + numUndos == gMoves.prevStates.size &&
              ! (gMovesUndo.prevStates, gMoves.prevStates.drop(numUndos)).zipped.exists(_.v != _.v)
      }
    })
  }
}
