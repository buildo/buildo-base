name := "nozzle-example"

scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-encoding",
  "utf8")

resolvers ++= Seq(
  "buildo mvn" at "https://raw.github.com/buildo/mvn/master/releases",
  "bintray buildo/maven" at "http://dl.bintray.com/buildo/maven"
)

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    "io.spray"       %% "spray-json"              % "1.3.2"
  )
}

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
