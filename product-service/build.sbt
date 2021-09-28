val scala3Version = "3.0.2"

lazy val productService = project
  .in(file("."))
  .settings(
    name := "product-service",
    version := "0.1.0-SNAPSHOT",
    organization := "com.example",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      Libs.zio,
      Libs.zioStream,
      Libs.zioKafka,
      Libs.zioJson,
      Libs.zioConfig,
      Libs.zioConfigTypesafe,
      Libs.logback
    )
  )
