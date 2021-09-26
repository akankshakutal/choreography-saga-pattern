package com.example

import zio.*
import zio.config.*
import zio.config.typesafe.*
import zio.config.magnolia.*

case class Topics(
    orderPlaced: String,
    paymentFailed: String,
    productAvailed: String,
    productAvailFailed: String
)

case class KafkaSettings(
    bootstrapServers: List[String],
    consumerGroupIdPrefix: String
)

case class Settings(topics: Topics, kafkaSettings: KafkaSettings)

object Settings {
  private val configDescr: ConfigDescriptor[Settings] =
    descriptor[Settings]

  def live(args: List[String]) = ZLayer.fromEffect(for {
    defaultSource <- TypesafeConfigSource.fromDefaultLoader.mapError(_.message)
    argsSource = ConfigSource.fromCommandLineArgs(args, Some('.'), Some(','))
    envSource <- ConfigSource
      .fromSystemEnvLive(Some('_'), Some(','))
      .mapError(_.getMessage)
    conf <- ZIO
      .fromEither(
        read(
          configDescr from (argsSource <> envSource <> defaultSource)
        )
      )
      .mapError(_.prettyPrint('.'))
  } yield conf)
}
