organization  := "io.buildo"

name := "base"

version := "0.4.2"

scalaVersion  := "2.11.7"

scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-encoding",
  "utf8")

resolvers ++= Seq(
  "buildo mvn" at "https://raw.github.com/buildo/mvn/master/releases"
)

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    "io.buildo"      %% "spray-autoproductformat" % "0.2",
    "io.buildo"      %% "ingredients-logging"    % "0.5.3",
    "io.buildo"      %% "ingredients-jsend"      % "0.3",
    "org.slf4j"      %  "slf4j-api"     % "1.7.7",
    "com.typesafe"   %  "config"        % "1.2.1",
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "io.spray"       %% "spray-can"     % sprayV,
    "io.spray"       %% "spray-routing-shapeless2" % sprayV,
    "io.spray"       %% "spray-httpx"   % sprayV,
    "org.scalaz"     %%  "scalaz-core"  % "7.1.1"
  )
}

Boilerplate.settings

publishTo := Some(Resolver.file("file", new File("releases")))

