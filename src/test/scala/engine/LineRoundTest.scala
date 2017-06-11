package engine

import org.junit.Test
import org.scalacheck.Arbitrary._
import org.scalacheck.{Gen, Prop}
import org.scalatest.junit.JUnitSuite
import org.scalatest.prop.Checkers

class LineRoundTest extends JUnitSuite with Checkers {
  @Test
  def noIdenticalTiles() {
    val in: List[Int] = 1::2::4::Nil
    assertResult(in)(LineRound.merge_tiles(in.toStream).toList)
  }

  @Test
  def identicalTilesCannotBeMerged() {
    val in: List[Int] = 1::2::4::2::Nil
    assertResult(in)(LineRound.merge_tiles(in.toStream).toList)
  }

  @Test
  def avoidCascadindMergedTiles() {
    val in: List[Int] = 1::1::2::4::Nil
    val ou: List[Int] = 2::2::4::Nil
    assertResult(ou)(LineRound.merge_tiles(in.toStream).toList)
  }

  @Test
  def mergeLeftSideFirst() {
    val in: List[Int] = 1::1::1::Nil
    val ou: List[Int] = 2::1::Nil
    assertResult(ou)(LineRound.merge_tiles(in.toStream).toList)
  }

  @Test
  def mutltipleMergesOnSameValue() {
    val in: List[Int] = 1::1::1::1::Nil
    val ou: List[Int] = 2::2::Nil
    assertResult(ou)(LineRound.merge_tiles(in.toStream).toList)
  }

  @Test
  def doMergeEvenAtTheEnd() {
    val in: List[Int] = 1::2::1::1::Nil
    val ou: List[Int] = 1::2::2::Nil
    assertResult(ou)(LineRound.merge_tiles(in.toStream).toList)
  }

  @Test
  def propertySummedValueNotImpacted() {
    check { l: List[Int] =>
      l.fold(0)(_ + _) == LineRound.merge_tiles(l.toStream).fold(0)(_ + _)
    }
  }

  @Test
  def propertyShorterOrSameAsInput() {
    check { l: List[Int] =>
      l.size >= LineRound.merge_tiles(l.toStream).size
    }
  }

  @Test
  def propertyDoNotHoldOnInfiniteStreams() {
    val inputGen = for {
      l   <- arbitrary[List[Int]] suchThat (_.size > 0)
      num <- Gen.choose(0, 1000)
    } yield (l, num)

    check(Prop.forAll(inputGen) {gens: (List[Int], Int) => gens match {
      case (l, num) => LineRound.merge_tiles(Stream.continually(l.toStream).flatten).take(num).size == num
    }})
  }
}
