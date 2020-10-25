name := """skkserv-scala"""
version := """0.1"""

run / fork := true

scalaVersion := """2.13.3"""
scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused"
)

assembly / assemblyJarName := "skkserv-scala.jar"

run / javaOptions ++= Seq(
  "-Xmx48M"
)

Compile / scalaSource := baseDirectory.value / "src"
Test / scalaSource := baseDirectory.value / "test"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % "0.13.0")

enablePlugins(BuildInfoPlugin)
buildInfoKeys := Seq[BuildInfoKey](name, version)
buildInfoPackage := """buildinfo"""

resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"
libraryDependencies += ("org.scalactic" %% "scalactic" % "3.2.2")
libraryDependencies += ("org.scalatest" %% "scalatest" % "3.2.2" % "test")
Test / logBuffered := false
Test / parallelExecution := false
