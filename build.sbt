name := "PlexMovies"

version := "1.0"

lazy val `plexmovies` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.4"

libraryDependencies ++= Seq( jdbc , anorm , cache , ws )

libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "1.1.4"

libraryDependencies += "com.netaporter" %% "scala-uri" % "0.4.6"

libraryDependencies += "org.webjars" % "requirejs" % "2.1.17"

libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23"

libraryDependencies += "org.webjars" %% "webjars-play" % "2.3.0-2"

libraryDependencies += "org.webjars" % "bootstrap" % "3.0.2"

libraryDependencies += "org.webjars" % "underscorejs" % "1.8.3"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

fork in run := true

includeFilter in (Assets, LessKeys.less) := "*.less"