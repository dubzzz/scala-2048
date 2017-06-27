package utility.random

import org.junit.Test
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Gen, Prop}
import org.scalatest.junit.JUnitSuite
import org.scalatest.prop.Checkers

class IncrementalInput(val stream: Stream[Int]) extends RandomGenerator[Int] {
  override def next(): (Int, IncrementalInput) =
    (stream.head, new IncrementalInput(stream.tail))
  override def equals(o: Any): Boolean = o match {
    case o: IncrementalInput => stream.head == o.stream.head
    case _ => false
  }
  def isEmpty: Boolean = stream.isEmpty
}

object IncrementalInput {
  def infinite(offset: Int): IncrementalInput = {
    def go(prev: Int): Stream[Int] = {
      val current = if (prev == Int.MaxValue) Int.MinValue else prev +1
      if (current == offset) throw new IllegalStateException("Too many states have been evaluating, check for infinite loop")
      current #:: go(current)
    }
    new IncrementalInput(offset #:: go(offset))
  }
}

class UniformDistributionTest extends JUnitSuite with Checkers {
  val MIN_LENGTH = 0
  val MAX_LENGTH = 1000
  def validWithin(min: Int, max: Int)(value: Int) = value >= min && value <= max
  def validLength(from: Int)(length: Int) =
      validWithin(MIN_LENGTH, MAX_LENGTH)(length) &&
      from <= length + from

  @Test
  def propertyAlwaysInTheRange() {
    def validTo(from: Int)(to: Int) = from <= to
    def validInput(in: (Int, Int, Int)) = validTo(in._2)(in._3)

    val inputGen = for {
      offset <- arbitrary[Int]
      from <- arbitrary[Int]
      to <- arbitrary[Int] suchThat (validTo(from)(_))
    } yield (offset, from, to)

    check(Prop.forAll(inputGen suchThat (validInput(_))) {
      case (offset, from, to) if validInput((offset, from, to)) => {
        val (out, _) = UniformDistribution.inRange(from, to)(IncrementalInput.infinite(offset))
        out >= from && out <= to
      }
      case _ => true
    })
  }

  @Test
  def propertyCanGenerateWhateverInTheRange() {
    def validTarget(from: Int, length: Int) = validWithin(from, from + length)(_)
    def validInput(in: (Int, Int, Int, Int)) =
        validLength(in._2)(in._3) &&
        validTarget(in._2, in._3)(in._4)

    val inputGen = for {
      offset <- arbitrary[Int]
      from <- arbitrary[Int]
      length <- Gen.choose(0, 1000) suchThat (validLength(from)(_))
      target <- Gen.choose(from, from + length)
    } yield (offset, from, length, target)

    check(Prop.forAll(inputGen suchThat (validInput(_))) {
      case (offset, from, length, target) if validInput((offset, from, length, target)) => {
        var found = false
        var rng = new IncrementalInput(IncrementalInput.infinite(offset).stream.take(2*length +1)) //twice the length should always be enough (+1 to avoid length = 0)
        while (! found) {
          val (out, nrng) = UniformDistribution.inRange(from, from + length)(rng)
          rng = nrng.asInstanceOf[IncrementalInput]
          found = (out == target)
        }
        found
      }
      case _ => true
    })
  }

  @Test
  def propertyEvenlyDistributed() {
    // UniformDistribution does even better (implementation specific)
    // for all range of N values, for M times N drawn of consecutives integers (i,i+1,i+2,...),
    // it will return exactly M times each of the N values

    val MIN_NUM = 1
    val MAX_NUM = 100
    def validNum = validWithin(MIN_NUM, MAX_NUM)(_)
    def validInput(in: (Int, Int, Int, Int)) =
        validLength(in._2)(in._3) &&
        validNum(in._4)

    val inputGen = for {
      offset <- arbitrary[Int]
      from <- arbitrary[Int]
      length <- Gen.choose(MIN_LENGTH, MAX_LENGTH) suchThat (validLength(from)(_))
      num <- Gen.choose(MIN_NUM, MAX_NUM)
    } yield (offset, from, length, num)

    check(Prop.forAll(inputGen suchThat (validInput(_))) {
      case (offset, from, length, num) if validInput((offset, from, length, num)) => {
        var gen = IncrementalInput.infinite(offset)
        val numRuns = num * (length +1)
        val buckets: Array[Int] = Array.ofDim[Int](length +1)
        for (i <- 0 to (numRuns -1)) {
          val (v, ngen) = UniformDistribution.inRange(from, from + length)(gen)
          gen = ngen.asInstanceOf[IncrementalInput]
          buckets(v - from) += 1
        }
        buckets.fold(0)(Math.max) == buckets.fold(Int.MaxValue)(Math.min)
      }
    })
  }

  @Test
  def propertyFrequencyEquivalentToFlattenOf() {
    def arityOf(e: (Int, Int)) = e._1
    def valueOf(e: (Int, Int)) = e._2
    
    val MIN_ARITY = 0
    val MAX_ARITY = 10
    def validEntry(e: (Int,Int)) = validWithin(MIN_ARITY, MAX_ARITY)(arityOf(e))
    def validEntries(es: List[(Int,Int)]) = es.foldLeft(0)(_ + arityOf(_)) > 0 && ! es.exists(! validEntry(_))
    def validInput(in: (Int, List[(Int,Int)])) = validEntries(in._2)
    
    val entryGen = for {
      arity <- Gen.choose(MIN_ARITY, MAX_ARITY)
      value <- arbitrary[Int]
    } yield (arity, value)

    val inputGen = for {
      offset <- arbitrary[Int]
      entries <- Gen.listOf(entryGen) suchThat (validEntries _)
    } yield (offset, entries)

    check(Prop.forAll(inputGen suchThat (validInput(_))) {
      case (offset, entries) if validInput((offset, entries)) => {
          var gen = IncrementalInput.infinite(offset)
          val flattenEq: List[Int] = entries.flatMap(e => Stream.continually(valueOf(e)).take(arityOf(e)).toList)
          val withOf = UniformDistribution.of(flattenEq.head, flattenEq.tail:_*)(gen)
          val withFrequency = UniformDistribution.frequency(entries:_*)(gen)
          withOf == withFrequency
      }
      case _ => true
    })
  }
}
