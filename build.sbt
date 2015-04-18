name := "PlexMovies"

version := "1.0"

lazy val `plexmovies` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

//resolvers += "rediscala" at "http://dl.bintray.com/etaty/maven"

libraryDependencies ++= Seq( jdbc , anorm , cache , ws )

libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "1.1.4"

libraryDependencies += "com.netaporter" %% "scala-uri" % "0.4.6"

//libraryDependencies += "com.etaty.rediscala" %% "rediscala" % "1.4.0"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

fork in run := true