package engine

sealed trait Direction
case object Left extends Direction
case object Right extends Direction
case object Up extends Direction
case object Down extends Direction

object Direction {
  def nextL(dir: Direction): Direction = dir match {
    case Left  => Down
    case Up    => Left
    case Right => Up
    case Down  => Right
  }
  def nextR(dir: Direction): Direction = dir match {
    case Left  => Up
    case Up    => Right
    case Right => Down
    case Down  => Left
  }
  def opposite(dir: Direction): Direction = Direction.nextR(Direction.nextR(dir))
}