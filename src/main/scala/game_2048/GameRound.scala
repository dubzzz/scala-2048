package game_2048

object GameRound {
  def merge_next(s: Stream[Int]): Stream[Int] = {
    def go(c: Stream[Int], prev: Option[Int]): Stream[Int] = {
      if (c.isEmpty) {
        if (prev.isEmpty) Stream.empty[Int]
        else prev.get #:: Stream.empty[Int]
      }
      else if (prev.map(_ == c.head).getOrElse(false)) (c.head +1) #:: go(c.tail, None)
      else if (prev.isEmpty) go(c.tail, Some(c.head))
      else prev.get #:: go(c.tail, Some(c.head))
    }
    if (s.isEmpty) Stream.empty[Int]
    else go(s.tail, s.headOption)
  }

  def play_move(s: Stream[Int], length: Int): Stream[Int] = {
    merge_next(s.filter(_ != 0))
        .append(Stream.continually(0))
        .take(4)
  }

  def main(args: Array[String]): Unit = {
    val input = 1::1::1::3::Nil
    val output: List[Int] = play_move(input.toStream, 4).toList
    println(output)
  }
}
