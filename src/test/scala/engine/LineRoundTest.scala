package engine

import org.junit.Test
import org.scalacheck.Arbitrary._
import org.scalacheck.{Gen, Prop}
import org.scalatest.junit.JUnitSuite
import org.scalatest.prop.Checkers

import scala.annotation.tailrec

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
      l   <- arbitrary[List[Int]] suchThat (! _.isEmpty)
      num <- Gen.choose(0, 1000)
    } yield (l, num)

    check(Prop.forAll(inputGen) {gens: (List[Int], Int) => gens match {
      case (l, num) => LineRound.merge_tiles(Stream.continually(l.toStream).flatten).take(num).size == num
    }})
  }

  @Test
  def propertyForgedInput() {
    def mergeEquals(l: List[(Int, Int)]): List[(Int,Int)] = {
      @tailrec def go(s: List[(Int, Int)], acc: List[(Int, Int)]): List[(Int, Int)] = s match {
        case Nil                       => acc
        case t :: Nil                  => t :: acc
        case (v1, n1) :: (v2, n2) :: q => if (v1 == v2) go((v1, n1 + n2) :: q, acc)
        else          go((v2, n2) :: q, (v1, n1) :: acc)
      }
      @tailrec def rev[A](s: List[A], acc: List[A]): List[A] = s match {
        case Nil     => acc
        case t :: q  => rev(q, t :: acc)
      }
      rev(go(l, Nil), Nil)
    }
    def inputStreamFor(l: List[(Int, Int)]): Stream[Int] = {
      l.toStream.map(e => e match { case (value, num) => Stream.continually(value).take(num) }).flatten
    }
    def outputStreamFor(l: List[(Int, Int)]): Stream[Int] = {
      l.toStream
        .map(e => e match { case (value, num) =>
          Stream.continually(2*value).take(num/2)
            .append(Stream.continually(value).take(num%2))
        })
        .flatten
    }

    val coupleGen = for {
      value <- Gen.oneOf(0,2,4,8,16,32,64,128,256,512,1024,2048)
      num   <- Gen.choose(1, 10)
    } yield (value, num)

    val inputGen = for {
      l   <- Gen.containerOf[List,(Int,Int)](coupleGen)
    } yield (l)

    check(Prop.forAll(inputGen) {l: List[(Int, Int)] =>
      LineRound.merge_tiles(inputStreamFor(l)) == outputStreamFor(mergeEquals(l))
    })
  }
}
