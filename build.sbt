name := "nozzle"

version := "0.11.0"

scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-encoding",
  "utf8"
)

resolvers ++= Seq(
  "buildo mvn" at "https://raw.github.com/buildo/mvn/master/releases",
  "bintray buildo/maven" at "http://dl.bintray.com/buildo/maven"
)

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    "com.typesafe.akka" %% "akka-actor"          % akkaV,
    "io.spray"       %% "spray-can"              % sprayV,
    "io.spray"       %% "spray-routing-shapeless2" % sprayV,
    "io.spray"       %% "spray-httpx"            % sprayV,
    "io.spray"       %% "spray-json"             % "1.3.2",
    "io.buildo"      %% "spray-autoproductformat" % "0.4.0",
    "org.scalaz"     %% "scalaz-core"            % "7.2.0"
  )
}

Boilerplate.settings

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

bintrayOrganization := Some("buildo")

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
