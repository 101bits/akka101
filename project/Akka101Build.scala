import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm
import sbt.Keys._
import sbt._

object Akka101Build extends Build {
  val akkaVersion = "2.4.0"
  val scalaVer = "2.11.7"

  lazy val commonSettings = Defaults.coreDefaultSettings ++ multiJvmSettings ++ Seq(
    organization := "com.learner",
    version := "0.1.0",
    scalaVersion := scalaVer,
    resolvers ++= Seq(
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "org.scalatest" %% "scalatest" % "2.2.5",
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion % "test"
    )
  )

  lazy val test_harness = project.settings(commonSettings: _*)

  lazy val cluster_simple = project
    .settings(commonSettings: _*) configs MultiJvm

  lazy val persistence = project
    .dependsOn(test_harness % "test->test")
    .settings(resolvers ++= Seq("dnvriend at bintray" at "http://dl.bintray.com/dnvriend/maven"))
    .settings(commonSettings: _*)
    .settings(libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-persistence" % "2.4-SNAPSHOT",
      "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.1.5" % "test"
    ))

  lazy val multiJvmSettings = SbtMultiJvm.multiJvmSettings ++ Seq(
    version := akkaVersion,
    scalaVersion := scalaVer,
    scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-target:jvm-1.6", "-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint"),
    javacOptions in Compile ++= Seq("-source", "1.6", "-target", "1.6", "-Xlint:unchecked", "-Xlint:deprecation"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion,
      "org.scalatest" %% "scalatest" % "2.2.5" % "test",
      "org.fusesource" % "sigar" % "1.6.4"),
    javaOptions in run ++= Seq(
      "-Djava.library.path=./sigar",
      "-Xms128m", "-Xmx1024m"),
    Keys.fork in run := true,
    // make sure that MultiJvm test are compiled by the default test compilation
    compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
    // disable parallel tests
    parallelExecution in Test := false,
    // make sure that MultiJvm tests are executed by the default test target,
    // and combine the results from ordinary test and multi-jvm tests
    executeTests in Test <<= (executeTests in Test, executeTests in MultiJvm) map {
      case (testResults, multiNodeResults) =>
        val overall =
          if (testResults.overall.id < multiNodeResults.overall.id)
            multiNodeResults.overall
          else
            testResults.overall
        Tests.Output(overall,
          testResults.events ++ multiNodeResults.events,
          testResults.summaries ++ multiNodeResults.summaries)
    }
  )
}