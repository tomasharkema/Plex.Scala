name := "PlexMovies"

version := "1.0"

lazy val `plexmovies` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( jdbc , anorm , cache , ws )

libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "1.1.4"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

fork in run := true