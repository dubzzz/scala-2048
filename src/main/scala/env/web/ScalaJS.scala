package env.web

/**
  * Created by Nicolas DUBIEN on 15/06/2017.
  */

import engine.{Down, GameManager, Left, Right, State2048, Up}
import org.scalajs.dom
import org.scalajs.dom.EventTarget
import org.scalajs.dom.raw.TouchEvent
import org.singlespaced.d3js.{Selection, d3}
import utility.random.MersenneTwister

import scala.scalajs.js

object ScalaJS extends js.JSApp {
  val numTiles   =   4
  val areaSize   = 400
  val tileMargin =  10
  val tileSize   = (areaSize - (numTiles -1) * tileMargin) / numTiles
  val fontSize   =  50

  val svgSize    = areaSize

  type TileArea = Selection[EventTarget]
  type TileEntity = (Int, Option[TileArea])

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

  def emptyTiles(size: Int) = Array.tabulate[TileEntity](size, size)((y,x) => (0, Option.empty[TileArea]))

  def updateTiles(area: Selection[dom.EventTarget], state: Array[Array[TileEntity]], next: Array[Array[Int]]): Array[Array[TileEntity]] = {
    Array.tabulate(next.size, next.size)((y,x) =>
      if (next(y)(x) == state(y)(x)._1) state(y)(x)
      else {
        state(y)(x)._2.map(destroyTile)
        (next(y)(x), appendIfTile(area, next, x, y))
      })
  }

  def drawUnderlyingGrid(area: Selection[EventTarget]): Array[Array[TileArea]] = {
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
  }

  def updateHash(seed: Int, numNewGames: Int, history: String): Unit = {
    dom.window.location.hash = s"#seed=${seed}&id=${numNewGames}&history=${history}"
  }

  def main(): Unit = {
    val svg: Selection[EventTarget] = d3.select("#playground").append("svg")
        .attr("width", s"${svgSize}px")
        .attr("height", s"${svgSize}px")
    val area = svg.append("g")

    var seed = System.currentTimeMillis.toInt
    var numNewGames = 0
    var game = GameManager.of(
      MersenneTwister.of(seed),
      State2048.newGame(_, numTiles))

    val initialHash = dom.window.location.hash
    if (! initialHash.isEmpty) {
      var history = "";
      initialHash.substring(1)
        .split('&')
        .map(item => (item.takeWhile(_ != '='), item.dropWhile(_ != '=').substring(1)))
        .foreach((entity) => {
          val key = entity._1
          val value = entity._2
          if (key == "seed") {
            seed = value.toInt
          }
          else if (key == "id") {
            numNewGames = value.toInt
          }
          else if (key == "history") {
            history = value;
          }
        })
      game = GameManager.of(
        MersenneTwister.of(seed),
        State2048.newGame(_, numTiles))
      game = Stream.from(0).take(numNewGames).foldLeft(game)((game, _) => game.newGame())
        .parse(history)
        .redoAll()
    }

    updateHash(seed, numNewGames, game.stringify())
    drawUnderlyingGrid(area)

    var tiles = emptyTiles(numTiles)
    tiles = updateTiles(area, tiles, game.state.grid)

    dom.document.getElementById("new-game").addEventListener("click", (e: EventTarget) => {
      numNewGames += 1
      game = game.newGame()
      updateHash(seed, numNewGames, game.stringify())
      tiles = updateTiles(area, tiles, game.state.grid)
    })
    dom.document.getElementById("undo-move").addEventListener("click", (e: EventTarget) => {
      game = game.undo().getOrElse(game)
      updateHash(seed, numNewGames, game.stringify())
      tiles = updateTiles(area, tiles, game.state.grid)
    })
    dom.document.getElementById("redo-move").addEventListener("click", (e: EventTarget) => {
      game = game.redo().getOrElse(game)
      updateHash(seed, numNewGames, game.stringify())
      tiles = updateTiles(area, tiles, game.state.grid)
    })

    var startTouch: (Double, Double) = (0.0, 0.0)
    dom.document.getElementsByTagName("svg")(0).addEventListener("touchstart", (e: TouchEvent) => {
      startTouch = (e.changedTouches(0).pageX, e.changedTouches(0).pageY)
    }, false)
    dom.document.getElementsByTagName("svg")(0).addEventListener("touchend", (e: TouchEvent) => {
      val endTouch = (e.changedTouches(0).pageX, e.changedTouches(0).pageY)
      val delta = (endTouch._1 - startTouch._1, endTouch._2 - startTouch._2)
      if (2 * Math.abs(delta._1) < Math.abs(delta._2)) {
        if (delta._2 > 0) game = game.next(Down).getOrElse(game)
        else game = game.next(Up).getOrElse(game)
        updateHash(seed, numNewGames, game.stringify())
        tiles = updateTiles(area, tiles, game.state.grid)
      }
      else if (2 * Math.abs(delta._2) < Math.abs(delta._1)) {
        if (delta._1 > 0) game = game.next(Right).getOrElse(game)
        else game = game.next(Left).getOrElse(game)
        updateHash(seed, numNewGames, game.stringify())
        tiles = updateTiles(area, tiles, game.state.grid)
      }
    }, false)

    dom.window.onkeydown = {(e: dom.KeyboardEvent) => e.keyCode match {
      case 37 /*left*/ => {
        game = game.next(Left).getOrElse(game)
        updateHash(seed, numNewGames, game.stringify())
        tiles = updateTiles(area, tiles, game.state.grid)
      }
      case 38 /*up*/ => {
        game = game.next(Up).getOrElse(game)
        updateHash(seed, numNewGames, game.stringify())
        tiles = updateTiles(area, tiles, game.state.grid)
      }
      case 39 /*right*/ => {
        game = game.next(Right).getOrElse(game)
        updateHash(seed, numNewGames, game.stringify())
        tiles = updateTiles(area, tiles, game.state.grid)
      }
      case 40 /*down*/ => {
        game = game.next(Down).getOrElse(game)
        updateHash(seed, numNewGames, game.stringify())
        tiles = updateTiles(area, tiles, game.state.grid)
      }
      case _ => ()}}
  }
}