package env.web

/**
  * Created by Nicolas DUBIEN on 15/06/2017.
  */

import org.singlespaced.d3js.d3
import scala.scalajs.js

object ScalaJS extends js.JSApp {
  def main(): Unit = {
    D3Board.build(d3.select("#playground"))
  }
}
