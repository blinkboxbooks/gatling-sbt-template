import io.gatling.sbt.GatlingPlugin

scalaVersion := "2.11.4"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7", "-Xlint")

enablePlugins(GatlingPlugin)

libraryDependencies ++= Seq(
  "com.blinkbox.books"       %% "common-scala-test"         % "0.3.0" % Test,
  "io.gatling.highcharts"    % "gatling-charts-highcharts"  % "2.1.3" % Test,
  "io.gatling"               % "gatling-test-framework"     % "2.1.3" % Test
)