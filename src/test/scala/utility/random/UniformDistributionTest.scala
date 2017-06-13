package utility.random

import org.junit.Test
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Gen, Prop}
import org.scalatest.junit.JUnitSuite
import org.scalatest.prop.Checkers

class IncrementalInput(val stream: Stream[Int]) extends RandomGenerator[Int] {
  override def next(): (Int, IncrementalInput) =
    (stream.head, new IncrementalInput(stream.tail))
  def isEmpty(): Boolean =
    stream.isEmpty
}

object IncrementalInput {
  def of(from: Int, to: Int): IncrementalInput = {
    def go(cur: Long, tolong: Long): Stream[Int] =
      if (cur <= tolong) cur.asInstanceOf[Int] #:: go(cur + 1L, tolong)
      else Stream.empty[Int]
    new IncrementalInput(go(from.asInstanceOf[Long], to.asInstanceOf[Long]))
  }
  def of(): IncrementalInput =
    IncrementalInput.of(Int.MinValue, Int.MaxValue)

  def infinite(offset: Int): IncrementalInput = {
    def go(prev: Int): Stream[Int] = {
      val current = if (prev == Int.MaxValue) Int.MinValue else prev +1
      current #:: go(current)
    }
    new IncrementalInput(go(offset))
  }
}

class UniformDistributionTest extends JUnitSuite with Checkers {
  @Test
  def propertyAlwaysInTheRange() {
    val inputGen = for {
      from <- arbitrary[Int]
      to <- arbitrary[Int] suchThat (from <= _)
    } yield (from, to)

    check(Prop.forAll(inputGen) {inputGen: (Int, Int) => inputGen match {
      case (from, to) => {
        val (out, _) = UniformDistribution.nextInt(from, to)(IncrementalInput.of())
        out >= from && out <= to
      }
    }})
  }

  @Test
  def propertyCanGenerateWhateverInTheRange() {
    val inputGen = for {
      from <- arbitrary[Int]
      length <- Gen.choose(0, 1000) suchThat (from <= _ + from) //arbitrary[Int] suchThat (from <= _)
      target <- Gen.choose(from, from + length)
    } yield (from, length, target)

    check(Prop.forAll(inputGen) {inputGen: (Int, Int, Int) => inputGen match {
      case (from, length, target) => {
        var found = false
        var rng = IncrementalInput.of(0, 2*length +1) //twice the length should always be enough (+1 to avoid length = 0)
        while (! found && ! rng.isEmpty()) {
          try {
            val (out, nrng) = UniformDistribution.nextInt(from, from + length)(rng)
            rng = nrng.asInstanceOf[IncrementalInput]
            found = (out == target)
          }
          catch {
            case _: Throwable => ()
          }
        }
        found
      }
    }})
  }

  @Test
  def propertyEvenlyDistributed() {
    val inputGen = for {
      offset <- arbitrary[Int]
      from <- arbitrary[Int]
      length <- Gen.choose(0, 1000) suchThat (from <= _ + from)
      num <- Gen.choose(1, 100)
    } yield (offset, from, length, num)

    check(Prop.forAll(inputGen) {inputGen: (Int, Int, Int, Int) => inputGen match {
      case (offset, from, length, num) => {
        var gen = IncrementalInput.infinite(offset)
        val numRuns = num * (length +1)
        val buckets: Array[Int] = Array.ofDim[Int](length +1)
        for (i <- 0 to (numRuns -1)) {
          val (v, ngen) = UniformDistribution.nextInt(from, from + length)(gen)
          gen = ngen.asInstanceOf[IncrementalInput]
          buckets(v - from) += 1
        }
        buckets.fold(0)(Math.max) == buckets.fold(Int.MaxValue)(Math.min)
      }
    }})
  }
}
