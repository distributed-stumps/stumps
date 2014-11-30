import sbt._
import Keys._
import spray.revolver.RevolverPlugin._
import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._
 
object Resolvers {
}
 
object Dependencies {
   val appDependencies = Seq(
      "com.typesafe.akka" %% "akka-actor"              % "2.3.7",
      "com.typesafe.akka" %% "akka-remote"             % "2.3.7",
      "com.typesafe.akka" %% "akka-kernel"             % "2.3.7"
      //"com.github.scalable_tech"     %% "stumps-messages" % "0.0.1-SNAPSHOT"
   )
}
 
object BuildSettings {
 
   val buildOrganization = "com.github.scalable_tech"
   val appName = "stumps"
   val buildVersion = "0.0.1-SNAPSHOT"
   val buildScalaVersion = "2.11.4"
   val buildScalaOptions = Seq("-unchecked", "-deprecation", "-encoding", "utf8")
 
   import Resolvers._
   import Dependencies._
 
   val buildSettings = Defaults.defaultSettings ++ Seq(
      organization         := buildOrganization,
      version              := buildVersion,
      scalaVersion         := buildScalaVersion,
      libraryDependencies ++= appDependencies,
      scalacOptions        := buildScalaOptions,
      mainClass in Compile := Some("com.github.scalable_tech.stumps.boot.Kernel")
   ) ++ Revolver.settings ++ packageArchetype.akka_application
}
 
object ApplicationBuild extends Build {
 
   import BuildSettings._
 
   lazy val main = Project(
      appName,
      file("."),
      settings = buildSettings)
}
