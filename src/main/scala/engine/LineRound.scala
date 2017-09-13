package engine

class LineRound[A](val extract: A => Int, val merge: (A,A) => A, val nullTile: A) {

  /** Merge successive tiles having same value together
    *
    * @param s stream of tiles' values
    * @return merged version of s
    */
  def merge_tiles(s: Seq[A]): Stream[A] = s match {
    case Nil                                              => Stream.empty[A]
    case Seq(t)                                           => t #:: Stream.empty[A]
    case t1 #:: t2 #:: q  if (extract(t1) == extract(t2)) => merge(t1, t2) #:: merge_tiles(q)
    case t1 #:: t2 #:: q                                  => t1 #:: merge_tiles(t2 #:: q)
  }

  /** Move one line towards the left */
  def play_move(num: Int)(s: Stream[A]): Stream[A] =
    merge_tiles(s.filter(extract(_) != 0))
      .append(Stream.continually(nullTile))
      .take(num)

}

object LineRound {
  def ofInt(): LineRound[Int] =
    new LineRound[Int](a => a, (a, b) => a + b, 0)
}
