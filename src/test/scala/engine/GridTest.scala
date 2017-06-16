package engine

import org.junit.Test
import org.scalacheck.{Arbitrary, Gen, Prop}
import org.scalatest.junit.JUnitSuite
import org.scalatest.prop.Checkers

class GridTest extends JUnitSuite with Checkers {
  val test_grid: Array[Array[Int]] = Array(
    Array( 1, 2, 3, 4),
    Array( 5, 6, 7, 8),
    Array( 9,10,11,12),
    Array(13,14,15,16)
  )

  @Test
  def ofLeft() {
    val s = (
      (1 :: 2 :: 3 :: 4 :: Nil).toStream
        :: (5 :: 6 :: 7 :: 8 :: Nil).toStream
        :: (9 :: 10 :: 11 :: 12 :: Nil).toStream
        :: (13 :: 14 :: 15 :: 16 :: Nil).toStream
        :: Nil
      ).toStream
    assertResult(test_grid)(Grid.of(s, Left))
  }

  @Test
  def ofRight() {
    val s = (
      (16 :: 15 :: 14 :: 13 :: Nil).toStream
        :: (12 :: 11 :: 10 ::  9 :: Nil).toStream
        :: ( 8 ::  7 ::  6 ::  5 :: Nil).toStream
        :: ( 4 ::  3 ::  2 ::  1 :: Nil).toStream
        :: Nil
      ).toStream
    assertResult(test_grid)(Grid.of(s, Right))
  }

  @Test
  def ofUp() {
    val s = (
      ( 4 ::  8 :: 12 :: 16 :: Nil).toStream
        :: ( 3 ::  7 :: 11 :: 15 :: Nil).toStream
        :: ( 2 ::  6 :: 10 :: 14 :: Nil).toStream
        :: ( 1 ::  5 ::  9 :: 13 :: Nil).toStream
        :: Nil
      ).toStream
    assertResult(test_grid)(Grid.of(s, Up))
  }

  @Test
  def ofDown() {
    val s = (
      (13 ::  9 ::  5 ::  1 :: Nil).toStream
        :: (14 :: 10 ::  6 ::  2 :: Nil).toStream
        :: (15 :: 11 ::  7 ::  3 :: Nil).toStream
        :: (16 :: 12 ::  8 ::  4 :: Nil).toStream
        :: Nil
      ).toStream
    assertResult(test_grid)(Grid.of(s, Down))
  }

  @Test
  def toStreamsLeft() {
    val expected: List[Int] = 1::2::3::4::5::6::7::8::9::10::11::12::13::14::15::16::Nil
    assertResult(expected)(Grid.toStreams(test_grid, Left).flatten.toList)
  }

  @Test
  def toStreamsRight() {
    val expected: List[Int] = 16::15::14::13::12::11::10::9::8::7::6::5::4::3::2::1::Nil
    assertResult(expected)(Grid.toStreams(test_grid, Right).flatten.toList)
  }

  @Test
  def toStreamsUp() {
    val expected: List[Int] = 4::8::12::16::3::7::11::15::2::6::10::14::1::5::9::13::Nil
    assertResult(expected)(Grid.toStreams(test_grid, Up).flatten.toList)
  }

  @Test
  def toStreamsDown() {
    val expected: List[Int] = 13::9::5::1::14::10::6::2::15::11::7::3::16::12::8::4::Nil
    assertResult(expected)(Grid.toStreams(test_grid, Down).flatten.toList)
  }

  def listToStreams(gridDefinition: List[Int], chunkSize: Int): Stream[Stream[Int]] = {
    def go(s: Stream[Int]): Stream[Stream[Int]] = {
      if (s.isEmpty) Stream.empty[Stream[Int]]
      else s.take(chunkSize) #:: go(s.drop(chunkSize))
    }
    go(gridDefinition.toStream)
  }
  def listToGrid(gridDefinition: List[Int], chunkSize: Int): Array[Array[Int]] = {
    val flatGrid = gridDefinition.to[Array]
    Array.tabulate(chunkSize, chunkSize)((x: Int, y: Int) => flatGrid(x * chunkSize + y))
  }

  @Test
  def propertyOfThenToStreamsIsIdentity() {
    val genGridDefinition = for {
      n <- Gen.choose(1, 100)
      direction <- Gen.oneOf(Left, Right, Up, Down)
      gridDefinition <- Gen.listOfN(n * n, Arbitrary.arbitrary[Int])
    } yield (n, direction, gridDefinition)

    check(Prop.forAll(genGridDefinition) { data =>
      data match {
        case (n, direction, gridDefinition) =>
          val s = listToStreams(gridDefinition, n)
          Grid.toStreams(Grid.of(s, direction), direction) == s
      }
    })
  }

  @Test
  def propertyToStreamsThenOfIsIdentity() {
    val genGridDefinition = for {
      n <- Gen.choose(1, 100)
      direction <- Gen.oneOf(Left, Right, Up, Down)
      gridDefinition <- Gen.listOfN(n * n, Arbitrary.arbitrary[Int])
    } yield (n, direction, gridDefinition)

    check(Prop.forAll(genGridDefinition) { data =>
      data match {
        case (n, direction, gridDefinition) =>
          val g = listToGrid(gridDefinition, n)
          Grid.of(Grid.toStreams(g, direction), direction) == g
      }
    })
  }

  @Test
  def propertyOppositeDirectionOppositeStreams() {
    val genGridDefinition = for {
      n <- Gen.choose(1, 100)
      direction <- Gen.oneOf(Left, Right, Up, Down)
      gridDefinition <- Gen.listOfN(n * n, Arbitrary.arbitrary[Int])
    } yield (n, direction, gridDefinition)

    def rev() = ()

    check(Prop.forAll(genGridDefinition) { data =>
      data match {
        case (n, direction, gridDefinition) =>
          val g = listToGrid(gridDefinition, n)
          Grid.toStreams(g, direction).flatten == rev(Grid.toStreams(g, Direction.nextR(Direction.nextR(direction))).flatten)
      }
    })
  }
}