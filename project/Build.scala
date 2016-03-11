import sbt._
import Keys._
import bintray.BintrayKeys._

object NozzleBuild extends Build {
  lazy val commonSettings = seq(
    organization  := "io.buildo",
    scalaVersion  := "2.11.7"
  )

  lazy val nozzle = project.in(file("."))
    .settings(commonSettings: _*)

  lazy val example = project.in(file("example"))
    .settings(commonSettings: _*)
    .dependsOn(nozzle)
}
