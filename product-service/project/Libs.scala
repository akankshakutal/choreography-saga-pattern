import sbt._

object Libs {
  lazy val zioStream = "dev.zio" %% "zio-streams" % "1.0.10"
  lazy val zio = "dev.zio" %% "zio" % "1.0.10"
  lazy val zioKafka = "dev.zio" %% "zio-kafka" % "0.16.0"
  lazy val zioJson = "dev.zio" %% "zio-json" % "0.2.0-M1"
  lazy val zioConfig = "dev.zio" %% "zio-config-magnolia" % "1.0.10"
  lazy val zioConfigTypesafe = "dev.zio" %% "zio-config-typesafe" % "1.0.10"
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
}
