name := """skkserv-scala"""
version := """0.1"""

scalaVersion := """2.13.2"""
scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation")

scalaSource in Compile := baseDirectory.value / "src"
scalaSource in Test := baseDirectory.value / "test"

// enablePlugins(ScalaNativePlugin)

enablePlugins(BuildInfoPlugin)
buildInfoKeys := Seq[BuildInfoKey](name, version)
buildInfoPackage := """buildinfo"""

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0" % "test"
logBuffered in Test := false
parallelExecution in Test := false
