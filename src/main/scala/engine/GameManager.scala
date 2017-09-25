package engine

import utility.random.RandomGenerator

import scala.annotation.tailrec

object GameManager {
  private def safeToInt(repr: Option[String]): Option[Int] =
    try { repr.flatMap(s => Some(s.toInt)) }
    catch { case _: Throwable => None }

  def of[State <: GameState](seededRngBuilder: Int => RandomGenerator[Int], builder: RandomGenerator[Int] => State, seed: Int): GameManager[State] =
    new GameManager[State](seededRngBuilder, builder, seed)

  def of[State <: GameState](seededRngBuilder: Int => RandomGenerator[Int], builder: RandomGenerator[Int] => State, seed: Int, gens: Int): GameManager[State] =
    Stream.from(0).take(gens).foldLeft(new GameManager[State](seededRngBuilder, builder, seed))((game, _) => game.newGame())

  def of[State <: GameState](seededRngBuilder: Int => RandomGenerator[Int], builder: RandomGenerator[Int] => State, seed: Int, gens: Int, history: String): GameManager[State] =
    GameManager.of[State](seededRngBuilder, builder, seed, gens)
      .applyHistory(history)
      .redoAll()

  def of[State <: GameState](seededRngBuilder: Int => RandomGenerator[Int], builder: RandomGenerator[Int] => State, repr: String): GameManager[State] = {
    val raw = repr
      .split('&')
      .map(item => (item.takeWhile(_ != '='), item.dropWhile(_ != '=').substring(1)))
      .toMap
    GameManager.of[State](
      seededRngBuilder,
      builder,
      safeToInt(raw.get("seed")).getOrElse(0),
      safeToInt(raw.get("id")).getOrElse(0),
      raw.get("history").getOrElse(""))
  }
}

class GameManager[State <: GameState](val seededRngBuilder: Int => RandomGenerator[Int], val builder: RandomGenerator[Int] => State, val seed: Int, val gens: Int, val prevHistory: List[(State, Direction)], val nextMoves: List[Direction]) {
  def this(seededRngBuilder: Int => RandomGenerator[Int], builder: RandomGenerator[Int] => State, seed: Int) =
    this(seededRngBuilder, builder, seed, 0, (builder(seededRngBuilder(seed)), Left) :: Nil, Nil)

  def newGame(): GameManager[State] =
    new GameManager(seededRngBuilder, builder, seed, gens + 1, (builder(initialState.rng.next()._2), Left) :: Nil, Nil)

  def initialState = {
    @tailrec def go(l: List[State], s: State): State = l match {
      case Nil    => s
      case t :: q => go(q, t)
    }
    go(prevStates.tail, prevStates.head)
  }
  def prevStates = prevHistory.map(_._1)
  def state = prevStates.head

  def next(dir: Direction): Option[GameManager[State]] = {
    state.next(dir).map(nextS => new GameManager(seededRngBuilder, builder, seed, gens, (nextS.asInstanceOf[State], dir) :: prevHistory, Nil))
  }
  def undo(): Option[GameManager[State]] = {
    if (! prevStates.tail.isEmpty) Some(new GameManager(seededRngBuilder, builder, seed, gens, prevHistory.tail, prevHistory.head._2 :: nextMoves))
    else None
  }
  def redo(): Option[GameManager[State]] = {
    if (! nextMoves.isEmpty) state.next(nextMoves.head).map(nextS => new GameManager(seededRngBuilder, builder, seed, gens, (nextS.asInstanceOf[State], nextMoves.head) :: prevHistory, nextMoves.tail))
    else None
  }

  @tailrec
  final def undoAll(): GameManager[State] = undo() match {
    case None    => this
    case Some(g) => g.undoAll()
  }

  @tailrec
  final def redoAll(): GameManager[State] = redo() match {
    case None    => this
    case Some(g) => g.redoAll()
  }

  def stringifyHistory(): String =
    prevHistory.map(_._2).map(_ match {
      case Left  => 'L'
      case Right => 'R'
      case Down  => 'D'
      case Up    => 'U'
    }).foldLeft("")((s, c) => c + s)
      .substring(1)

  def stringify(): String = s"seed=${seed}&id=${gens}&history=${stringifyHistory}"

  def applyHistory(history: String): GameManager[State] =
    new GameManager(seededRngBuilder, builder, seed, gens,
      (initialState, Left) :: Nil,
      history.map(_ match {
        case 'L' => Left
        case 'R' => Right
        case 'U' => Up
        case  _  => Down
      }).to[List])
}
