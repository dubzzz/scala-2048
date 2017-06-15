enablePlugins(ScalaJSPlugin)

name := "scala-2048"

version := "1.0"

scalaVersion := "2.12.2"

scalaJSUseMainModuleInitializer := true

libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    "org.singlespaced" %%% "scalajs-d3" % "0.3.4",
    "junit" % "junit" % "4.12" % "test",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
)
