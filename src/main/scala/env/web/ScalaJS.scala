package env.web

/**
  * Created by Nicolas DUBIEN on 15/06/2017.
  */

import engine.{GameManager, State2048}
import org.scalajs.dom.EventTarget
import org.singlespaced.d3js.{Selection, d3}
import utility.random.MersenneTwister

import scala.scalajs.js

object ScalaJS extends js.JSApp {
  val svgHeight  = 450

  val numTiles   =   4
  val areaSize   = 400
  val tileMargin =  10
  val tileSize   = (areaSize - (numTiles -1) * tileMargin) / numTiles
  val fontSize   =  50

  type TileArea = Selection[EventTarget]

  def appendOneTile(area: Selection[EventTarget], grid: Array[Array[Int]], x: Int, y: Int): TileArea = {
    val px = tileMargin * x + tileSize * x + tileSize/2
    val py = tileMargin * y + tileSize * y + tileSize/2
    var g = area.append("g")
    var r = g.append("rect")
        .style("fill", "#ff0000")
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
  def appendTiles(area: Selection[EventTarget], grid: Array[Array[Int]]): Array[Array[Option[TileArea]]] = {
    Array.tabulate(grid.size, grid.size)((x,y) =>
      if (grid(y)(x) == 0) None
      else Some(appendOneTile(area, grid, x, y)))
  }
  def drawUnderlyingGrid(area: Selection[EventTarget]): Array[Array[TileArea]] = {
    Array.tabulate(numTiles,numTiles)((x,y) => {
      val px = tileMargin * x + tileSize * x + tileSize / 2
      val py = tileMargin * y + tileSize * y + tileSize / 2
      var g = area.append("g")
      var r = g.append("rect")
        .style("fill", "#ffdddd")
        .attr("x", px - tileSize / 2)
        .attr("y", py - tileSize / 2)
        .attr("width", tileSize)
        .attr("height", tileSize)
      g
    })
  }

  def main(): Unit = {
    val svg: Selection[EventTarget] = d3.select("body").append("svg")
        .attr("width", "100%")
        .attr("height", s"${svgHeight}px")
    val area = svg.append("g")
    var game = GameManager.of(
      MersenneTwister.of(System.currentTimeMillis.toInt),
      State2048.newGame(_, numTiles))

    drawUnderlyingGrid(area)
    appendTiles(area, game.state.grid)
  }
}