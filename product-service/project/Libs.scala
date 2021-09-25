import sbt._

object Libs {
  lazy val zioStream = "dev.zio" %% "zio-streams" % "1.0.12"
  lazy val zio = "dev.zio" %% "zio" % "1.0.12"
  lazy val zioKafka = "dev.zio" %% "zio-kafka" % "0.16.0"
}