name := "PlexMovies"

version := "1.0"

lazy val `plexmovies` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.4"

libraryDependencies ++= Seq( jdbc , anorm , cache , ws,
  "com.netaporter" %% "scala-uri" % "0.4.6",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23",
  "org.webjars" %% "webjars-play" % "2.3.0-2",
  "org.webjars" % "bootstrap" % "3.0.2",
  "org.webjars" % "underscorejs" % "1.8.3"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

fork in run := true

includeFilter in (Assets, LessKeys.less) := "*.less"