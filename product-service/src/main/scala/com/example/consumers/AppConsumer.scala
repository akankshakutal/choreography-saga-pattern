package com.example
package consumers

import zio.*
import zio.duration.*
import zio.kafka.consumer.Consumer
import zio.kafka.consumer.ConsumerSettings
import zio.clock.Clock
import zio.blocking.Blocking
import zio.kafka.consumer.diagnostics.Diagnostics

object AppConsumer {
  def apply(groupId: String, clientId: String): ZLayer[Has[
    Clock.Service
  ] & Has[Blocking.Service] & Has[Settings], Nothing, Has[Consumer]] =
    ZLayer.fromServiceManaged { (settings: Settings) =>
      Consumer
        .make(
          ConsumerSettings(settings.kafkaSettings.bootstrapServers)
            .withGroupId(
              settings.kafkaSettings.consumerGroupIdPrefix + groupId
            )
            .withClientId(clientId)
            .withCloseTimeout(30.seconds)
        )
    }.orDie
}
