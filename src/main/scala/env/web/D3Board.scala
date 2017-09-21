package env.web

import engine.{Down, Left, Right, Up}
import env.web.D3Board.{fontSize, numTiles, tileMargin, tileSize}
import mvc.BoardController
import org.scalajs.dom
import org.scalajs.dom.EventTarget
import org.scalajs.dom.raw.TouchEvent
import org.singlespaced.d3js.{Selection, d3}

class D3Board(val area: Selection[EventTarget]) {
  type TileArea = Selection[EventTarget]
  type TileEntity = (Int, Option[TileArea])
  var tiles: Array[Array[TileEntity]] = Array.tabulate[TileEntity](numTiles, numTiles)((y,x) => (0, Option.empty[TileArea]))
  var game: BoardController.Model = null
  var startTouch: (Double, Double) = (0.0, 0.0)

  def colorFor(value: Int): String = value match {
    case    2 => "#f9e08c"
    case    4 => "#ffcc49"
    case    8 => "#fcb514"
    case   16 => "#f99b0c"
    case   32 => "#ef6b00"
    case   64 => "#f95602"
    case  128 => "#f74902"
    case  256 => "#ef2b2d"
    case  512 => "#e5053a"
    case 1024 => "#d30547"
    case 2048 => "#af003d"
    case    _ => "#000000"
  }

  def appendTile(area: Selection[EventTarget], grid: Array[Array[Int]], x: Int, y: Int): TileArea = {
    val px = tileMargin * x + tileSize * x + tileSize/2
    val py = tileMargin * y + tileSize * y + tileSize/2
    var g = area.append("g")
    var r = g.append("rect")
      .style("fill", colorFor(grid(y)(x)))
      .attr("x", px)
      .attr("y", py)
      .attr("width", 0)
      .attr("height", 0)
    var t = g.append("text")
      .text(s"${grid(y)(x)}")
      .style("fill", "white")
      .style("font-size", s"${fontSize}px")
      .attr("text-anchor", "middle")
      .attr("x", px)
      .attr("y", py +20)
    r.transition("slide")
      .attr("x", px - tileSize / 2)
      .attr("y", py - tileSize / 2)
    r.transition("size")
      .attr("width", tileSize)
      .attr("height", tileSize)

    g
  }
  def appendIfTile(area: Selection[EventTarget], grid: Array[Array[Int]], x: Int, y: Int): Option[TileArea] =
    if (grid(y)(x) == 0) None
    else Some(appendTile(area, grid, x, y))

  def destroyTile(tile: TileArea) = tile.remove()

  def updateTiles(area: Selection[dom.EventTarget], next: Array[Array[Int]]) = {
    tiles = Array.tabulate(next.size, next.size)((y,x) =>
      if (next(y)(x) == tiles(y)(x)._1) tiles(y)(x)
      else {
        tiles(y)(x)._2.map(destroyTile)
        (next(y)(x), appendIfTile(area, next, x, y))
      })
  }

  def onUpdate(updatedGame: BoardController.Model) = {
    game = updatedGame
    dom.window.location.hash = s"#${game.stringify()}"
    updateTiles(area, game.state.grid)
  }

  def executeGameAction(action: BoardController.Model => Option[BoardController.Model]) =
    action(game).map(onUpdate(_))

  def registerClicks() = {
    dom.document.getElementById("new-game").addEventListener("click", (e: EventTarget) => executeGameAction(BoardController.newGame))
    dom.document.getElementById("undo-move").addEventListener("click", (e: EventTarget) => executeGameAction(BoardController.undo))
    dom.document.getElementById("redo-move").addEventListener("click", (e: EventTarget) => executeGameAction(BoardController.redo))
    dom.document.getElementById("replay-all").addEventListener("click", (e: EventTarget) => {
      onUpdate(game.undoAll())

      var replay = () => {}
      replay = () => {
        val ngame = game.redo()
        if (! ngame.isEmpty) {
          onUpdate(ngame.get)
          dom.window.setTimeout(replay, 100)
        }
      }
      dom.window.setTimeout(replay, 100)
    })
  }
  def registerTouch() = {
    dom.document.getElementsByTagName("svg")(0).addEventListener("touchstart", (e: TouchEvent) => {
      startTouch = (e.changedTouches(0).pageX, e.changedTouches(0).pageY)
    }, false)
    dom.document.getElementsByTagName("svg")(0).addEventListener("touchend", (e: TouchEvent) => {
      val endTouch = (e.changedTouches(0).pageX, e.changedTouches(0).pageY)
      val delta = (endTouch._1 - startTouch._1, endTouch._2 - startTouch._2)
      if (2 * Math.abs(delta._1) < Math.abs(delta._2)) {
        executeGameAction(BoardController.play(_, if (delta._2 > 0) Down else Up))
      }
      else if (2 * Math.abs(delta._2) < Math.abs(delta._1)) {
        executeGameAction(BoardController.play(_, if (delta._1 > 0) Right else Left))
      }
    }, false)
  }
  def registerKeyboard() = {
    dom.window.onkeydown = {(e: dom.KeyboardEvent) => e.keyCode match {
      case 37 /*left*/  => executeGameAction(BoardController.play(_, Left))
      case 38 /*up*/    => executeGameAction(BoardController.play(_, Up))
      case 39 /*right*/ => executeGameAction(BoardController.play(_, Right))
      case 40 /*down*/  => executeGameAction(BoardController.play(_, Down))
      case _ => ()}}
  }
  def init(): D3Board = {
    Array.tabulate(numTiles,numTiles)((y,x) => {
      val px = tileMargin * x + tileSize * x + tileSize / 2
      val py = tileMargin * y + tileSize * y + tileSize / 2
      var g = area.append("g")
      var r = g.append("rect")
        .style("fill", "#fcf5dc")
        .attr("x", px - tileSize / 2)
        .attr("y", py - tileSize / 2)
        .attr("width", tileSize)
        .attr("height", tileSize)
      g
    })

    onUpdate(
      if (dom.window.location.hash.isEmpty) BoardController.init(numTiles)
      else BoardController.load(numTiles, dom.window.location.hash))

    registerClicks()
    registerTouch()
    registerKeyboard()
    this
  }
}

object D3Board {
  val numTiles   =   4
  val areaSize   = 400
  val tileMargin =  10
  val tileSize   = (areaSize - (numTiles -1) * tileMargin) / numTiles
  val fontSize   =  50
  val svgSize    = areaSize

  def build(domElement: Selection[EventTarget]): D3Board = {
    val svg: Selection[EventTarget] = domElement.append("svg")
      .attr("width", s"${svgSize}px")
      .attr("height", s"${svgSize}px")
    val area = svg.append("g")

    new D3Board(area).init()
  }
}
