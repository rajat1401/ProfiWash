import Dependencies._
import com.typesafe.sbt.packager.MappingsHelper._

ThisBuild / scalaVersion := "2.13.12"
ThisBuild / organization := "com.bansal"
ThisProject / name := "wash-catalog-backend"
ThisBuild / version := "1.0-SNAPSHOT"

ThisBuild / resolvers += "Typesafe" at "https://repo.typesafe.com/typesafe/releases/"

enablePlugins(UniversalPlugin)

val assemblySettings = Seq(
  assembly / assemblyJarName := name.value + ".jar",
  assembly / assemblyMergeStrategy := {
    case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".txt" => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.concat
    case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".xml" => MergeStrategy.first
    case "application.conf" => MergeStrategy.concat
    case x =>
      val oldStrategy = (assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  }
)

lazy val dbDependencies= Seq(mySqlConnector, jdbc)

libraryDependencies ++= dbDependencies ++ Seq(ws, scalatest)

lazy val wash_catalog = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """ProfiWash""",
    commonSettings)
  .enablePlugins(PlayScala)
  .dependsOn(common, catalog)
  .aggregate(common, catalog)

lazy val catalog= (project in file("catalog"))
  .settings(
    name := "catalog",
    retrieveManaged := true,
    publish / skip := true,
    run / fork := true,
    commonSettings,
    assemblySettings,
    libraryDependencies += slickless
  ).dependsOn(common)

lazy val common = (project in file("common"))
  .settings(
    name := "common",
    publish / skip := true,
    retrieveManaged := true,
    commonSettings,
    libraryDependencies ++= Seq(
      slick, slickHikaricp, shapeless, playJson, evolutions, jacksonCore, jacksonDatabind, jacksonScala,
      playJsonJoda, playAhcWs
    )
  )