import sbt._
import sbt.Keys._
import play.sbt.PlayImport.guice

object Dependencies {

  lazy val commonSettings= Seq(
    libraryDependencies += guice,
    libraryDependencies += playTest,
    dependencyOverrides ++= Seq(guava),
  )

  val playVersion= "2.9.0"
  val playJsonVersion= "2.10.1"
  val slickVersion= "3.3.2"
  val akkaVersion= "2.8.3"
  val akkaHttpVersion= "10.5.3"
  val jacksonVersion= "2.15.2"

  val mySqlConnector= "mysql" % "mysql-connector-java" % "8.0.33"

  val playLogBack= "com.typesafe.play" %% "play-logback" % playVersion
  val playAhcWs= "com.typesafe.play" %% "play-ahc-ws" % playVersion
  val playJson= "com.typesafe.play" %% "play-json" % playJsonVersion
  val playJsonJoda= "com.typesafe.play" %% "play-json-joda" % playJsonVersion


  val slick= "com.typesafe.slick" %% "slick" % slickVersion
  val slickHikaricp= "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
  val slickless= "io.underscore" %% "slickless" % "0.3.6"
  val shapeless= "com.chuusai" %% "shapeless" % "2.3.10"

  val akkaActor= "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaSlf4j= "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
  val akkaStream= "com.typesafe.akka" %% "akka-stream" % akkaVersion
  val akkaHttp= "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
  val akkaHttpCore= "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion

  val scalatest= "org.scalatest" %% "scalatest" % "3.2.15"
  val playTest= "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
  val guava= "com.google.guava" %% "guava" % "33.0.0-jre"

  val jacksonDatabind= "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion
  val jacksonAnnotation= "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion
  val jacksonCore= "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion
  val jacksonScala= "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion

}
