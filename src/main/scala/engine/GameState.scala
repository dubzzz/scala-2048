package engine

import utility.random.{RandomGenerator, UniformDistribution}

class GameState(val rng: RandomGenerator[Int], val grid: Array[Array[Int]]) {
  def score(): Int =
    Grid.toStreams(grid).flatten.fold(0)(_ + _)

  def next(direction: Direction): Option[GameState] = {
    val current = Grid.toStreams(grid, direction)
    val afterMove = current.map(LineRound.play_move(grid.length))
    if (current == afterMove) Option.empty[GameState]
    else {
      val (afterAppend, nrng) = GameState.appendRand(rng, afterMove)
      Some(new GameState(nrng, Grid.of(afterAppend, direction)))
    }
  }
}

object GameState {
  def newGame(rng: RandomGenerator[Int], size: Int = 4): GameState = {
    val s = Grid.toStreams(Grid.empty(size))
    val (ns1, rng1) = appendRand(rng, s)
    val (ns2, rng2) = appendRand(rng, ns1)
    new GameState(rng2, Grid.of(ns2))
  }

  def appendRand(rng: RandomGenerator[Int], s: Stream[Stream[Int]]): (Stream[Stream[Int]], RandomGenerator[Int]) = {
    val numNulls = s.flatten.count(_ == 0)
    val (position, rng1) = UniformDistribution.nextInt(0, numNulls -1)(rng)
    val (guessValue, rng2) = UniformDistribution.nextInt(0, 9)(rng)
    val v = if (guessValue == 0) 4 else 2

    def replace_null(ss: Stream[Int], waitNulls: Int): Stream[Int] = {
      if (ss.isEmpty) Stream.empty[Int]
      else if (ss.head == 0) {
        if (waitNulls == 0) v #:: ss.tail
        else ss.head #:: replace_null(ss.tail, waitNulls -1)
      }
      else ss.head #:: replace_null(ss.tail, waitNulls)
    }
    def split_back(ss: Stream[Int], size: Int): Stream[Stream[Int]] = {
      if (ss.isEmpty) Stream.empty[Stream[Int]]
      else ss.take(size) #:: split_back(ss.drop(size), size)
    }
    (split_back(replace_null(s.flatten, position), s.size), rng2)
  }
}
