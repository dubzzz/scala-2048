package engine

import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.forAll

object LineRoundTest extends Properties("LineRound") {

  property("merge_2048: constant sum") = forAll { l: List[Int] =>
    l.fold(0)(_ + _) == LineRound.merge_2048(l.toStream).fold(0)(_ + _)
  }

  property("merge_2048: merged size inferior or equal to initial's") = forAll { l: List[Int] =>
    l.size >= LineRound.merge_2048(l.toStream).size
  }

  /*val myGen = for {
    l <- List[Int]
    num <- Gen.choose(0, 1000)
  } yield (l, num)

  property("merge_2048: partial evaluation ends") = forAll(myGen) { case (l: List[Int], num: Int) =>
    LineRound.merge_2048(Stream.continually(l.toStream).flatten).take(num).size == num
  }*/

}
