package utility.random

import org.junit.Test
import org.scalacheck.Arbitrary._
import org.scalacheck.{Gen, Prop}
import org.scalatest.junit.JUnitSuite
import org.scalatest.prop.Checkers

class ImperativeMersenneTwister {
  val N = MersenneTwister.N
  val M = MersenneTwister.M
  val R = MersenneTwister.R
  val A = MersenneTwister.A
  val F = MersenneTwister.F
  val U = MersenneTwister.U
  val S = MersenneTwister.S
  val B = MersenneTwister.B
  val T = MersenneTwister.T
  val C = MersenneTwister.C
  val L = MersenneTwister.L
  val MASK_LOWER = MersenneTwister.MASK_LOWER
  val MASK_UPPER = MersenneTwister.MASK_UPPER

  var mt: Array[Int] = Array.ofDim[Int](N)
  var index: Int = 0

  def init(seed: Int): ImperativeMersenneTwister = {
    mt(0) = seed
    for (i <- 1 to (N-1)) {
      mt(i) = F * (mt(i - 1) ^ (mt(i - 1) >> 30)) + i
    }
    index = N
    this
  }

  def twist(): ImperativeMersenneTwister = {
    for (i <- 0 to (N-1)) {
      var x = (mt(i) & MASK_UPPER) + (mt((i + 1) % N) & MASK_LOWER)
      var xA = x >> 1
      if ((x & 1) == 1) {
        xA ^= A
      }
      mt(i) = mt((i+M) % N) ^ xA
    }
    index = 0
    this
  }

  def run(): Int = {
    var y = 0
    var i = index
    if (index >= N) {
      twist()
      i = index
    }
    y = mt(i)
    index = i + 1
    y ^= (mt(i) >> U)
    y ^= (y << S) & B
    y ^= (y << T) & C
    y ^= (y >> L)
    y
  }
}

class MersenneTwisterTest extends JUnitSuite with Checkers {
  @Test
  def propertySameSeedSameStream() {
    val inputGen = for {
      seed   <- arbitrary[Int]
      offset <- Gen.choose(0, 2*MersenneTwister.N)
      num <- Gen.choose(0, 2*MersenneTwister.N)
    } yield (seed, offset, num)

    check(Prop.forAll(inputGen) { data: (Int, Int, Int) => data match {
      case (seed, offset, num) => MersenneTwister.stream(seed).drop(offset).take(num) == MersenneTwister.stream(seed).drop(offset).take(num)
    }})
  }

  @Test
  def propertySameAsItsImperativeImplementation() {
    val inputGen = for {
      seed   <- arbitrary[Int]
      offset <- Gen.choose(0, 2*MersenneTwister.N)
      num <- Gen.choose(0, 2*MersenneTwister.N)
    } yield (seed, offset, num)

    check(Prop.forAll(inputGen) { data: (Int, Int, Int) => data match {
      case (seed, offset, num) => {
        val tw: ImperativeMersenneTwister = new ImperativeMersenneTwister
        tw.init(seed)
        MersenneTwister.stream(seed).drop(offset).take(num) == Stream.tabulate(offset + num)(_ => tw.run).drop(offset)
      }
    }})
  }
}
