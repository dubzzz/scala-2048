package engine

object Grid {

  def empty(size: Int): Array[Array[Int]] =
    Array.ofDim[Int](size, size)

  def of(s: Stream[Stream[Int]], dir: Direction): Array[Array[Int]] = {
    val size: Int = s.size
    val flat_stream: Array[Int] = s.flatten.to[Array]
    dir match {
      case Left  => Array.tabulate(size, size)((x: Int, y: Int) => flat_stream(x * size + y))
      case Right => Array.tabulate(size, size)((x: Int, y: Int) => flat_stream((size -x -1) * size + (size -y -1)))
      case Up    => Array.tabulate(size, size)((x: Int, y: Int) => flat_stream(y * size + (size -x -1)))
      case Down  => Array.tabulate(size, size)((x: Int, y: Int) => flat_stream((size -y -1) * size + x))
    }
  }

  def main() = {
    val s = (
        (1 :: 2 :: 3 :: 4 :: Nil).toStream
        :: (5 :: 6 :: 7 :: 8 :: Nil).toStream
        :: (9 :: 10 :: 11 :: 12 :: Nil).toStream
        :: (13 :: 14 :: 15 :: 16 :: Nil).toStream
        :: Nil
        ).toStream

    println(Grid.of(s, Left))
  }

}
