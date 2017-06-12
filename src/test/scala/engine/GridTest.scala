package engine

import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.prop.Checkers

class GridTest extends JUnitSuite with Checkers {
  val s = (
    (1 :: 2 :: 3 :: 4 :: Nil).toStream
      :: (5 :: 6 :: 7 :: 8 :: Nil).toStream
      :: (9 :: 10 :: 11 :: 12 :: Nil).toStream
      :: (13 :: 14 :: 15 :: 16 :: Nil).toStream
      :: Nil
    ).toStream

  @Test
  def ofLeft() {
    val expected: Array[Array[Int]] = Array(
      Array( 1, 2, 3, 4),
      Array( 5, 6, 7, 8),
      Array( 9,10,11,12),
      Array(13,14,15,16)
    )
    assertResult(expected)(Grid.of(s, Left))
  }

  @Test
  def ofRight() {
    val expected: Array[Array[Int]] = Array(
      Array(16,15,14,13),
      Array(12,11,10, 9),
      Array( 8, 7, 6, 5),
      Array( 4, 3, 2, 1)
    )
    assertResult(expected)(Grid.of(s, Right))
  }

  @Test
  def ofUp() {
    val expected: Array[Array[Int]] = Array(
      Array( 4, 8,12,16),
      Array( 3, 7,11,15),
      Array( 2, 6,10,14),
      Array( 1, 5, 9,13)
    )
    assertResult(expected)(Grid.of(s, Up))
  }

  @Test
  def ofDown() {
    val expected: Array[Array[Int]] = Array(
      Array(13, 9, 5, 1),
      Array(14,10, 6, 2),
      Array(15,11, 7, 3),
      Array(16,12, 8, 4)
    )
    assertResult(expected)(Grid.of(s, Down))
  }
}