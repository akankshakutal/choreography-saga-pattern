package com.example
package consumers

import zio.*
import zio.kafka.consumer.Consumer
import zio.kafka.consumer.ConsumerSettings

object AppConsumer {
  def apply(groupId: String) =
    ZLayer.fromServiceManaged { (settings: Has[Settings]) =>
      Consumer
        .make(
          ConsumerSettings(settings.get.kafkaSettings.bootstrapServers)
            .withGroupId(
              settings.get.kafkaSettings.consumerGroupIdPrefix + groupId
            )
        )
    }.orDie
}