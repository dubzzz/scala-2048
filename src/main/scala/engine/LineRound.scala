package engine

object LineRound {
  def merge_2048(s: => Seq[Int]): Stream[Int] = s match {
    case Nil               => Stream.empty[Int]
    case Seq(t)            => t #:: Stream.empty[Int]
    case t1 #:: t2 #:: q   => if (t1 == t2) (2 * t1) #:: merge_2048(q)
                              else t1 #:: merge_2048(t2 #:: q)
  }
}
