package com.example

import zio.*
import zio.console.*
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.consumer.Consumer
import zio.blocking.Blocking
import zio.clock.Clock
import zio.kafka.producer.Producer
import zio.kafka.producer.ProducerSettings
import com.example.producers.SuccessProducer
import com.example.producers.FailureProducer
import com.example.consumers.AppConsumer

object Application extends App {
  def dependencies(args: List[String]) = 
    val settings = Settings.live(args).mapError(RuntimeException(_)).orDie
    val producerSettings = settings.map(s =>
      Has(ProducerSettings(s.get.kafkaSettings.bootstrapServers))
    )
    val producer = (producerSettings ++ Blocking.live) >>> Producer.live.orDie
    val successProducer = (producer ++ settings) >>> SuccessProducer.live
    val failureProducer = (producer ++ settings) >>> FailureProducer.live
    val productService = ProductService.live
    val orderPlacedConsumer =
      (Clock.live ++ Blocking.live ++ settings) >>> AppConsumer(
        "orderPlacedConsumer"
      )
    (productService ++ settings ++ successProducer ++ failureProducer ++ Clock.live ++ orderPlacedConsumer ++ Console.live) >>> OrderStream.live

  override def run(args: List[String]) =
    OrderStream.stream.orDie
    .provideSomeLayer(dependencies(args))
    .exitCode
}
