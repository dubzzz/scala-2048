package engine

object LineRound {

  /** Merge successive tiles having same value together
    *
    * @param s stream of tiles' values
    * @return merged version of s
    */
  def merge_tiles(s: => Seq[Int]): Stream[Int] = s match {
    case Nil               => Stream.empty[Int]
    case Seq(t)            => t #:: Stream.empty[Int]
    case t1 #:: t2 #:: q   => if (t1 == t2) (2 * t1) #:: merge_tiles(q)
                              else t1 #:: merge_tiles(t2 #:: q)
  }

}
