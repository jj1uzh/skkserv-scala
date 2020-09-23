name := """skkserv-scala"""
version := """0.1"""

scalaVersion := """2.13.3"""
scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation")
// scalacOptions ++= { if (isDotty.value) Seq("-source:3.0-migration") else Nil }

scalaSource in Compile := baseDirectory.value / "src"
scalaSource in Test := baseDirectory.value / "test"

// enablePlugins(ScalaNativePlugin)

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % "0.13.0")

enablePlugins(BuildInfoPlugin)
buildInfoKeys := Seq[BuildInfoKey](name, version)
buildInfoPackage := """buildinfo"""

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
libraryDependencies += ("org.scalactic" %% "scalactic" % "3.2.2")
libraryDependencies += ("org.scalatest" %% "scalatest" % "3.2.2" % "test")
logBuffered in Test := false
parallelExecution in Test := false
