package com.example

import zio.*
import zio.console.*
import zio.duration.*
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.consumer.Consumer
import zio.kafka.producer.Producer
import zio.kafka.producer.ProducerSettings
import com.example.producers.*
import com.example.consumers.AppConsumer
import com.example.messages.OrderPlaced
import zio.kafka.consumer.Subscription
import zio.blocking.Blocking

object Application extends App {
  override def run(args: List[String]) =
    val settings = Settings.live(args).mapError(RuntimeException(_)).orDie
    val producerSettings = settings.map(s =>
      Has(ProducerSettings(s.get.kafkaSettings.bootstrapServers))
    )
    val consumerSettings = settings.map(s =>
      Has(
        ConsumerSettings(s.get.kafkaSettings.bootstrapServers)
          .withGroupId(
            s.get.kafkaSettings.consumerGroupIdPrefix + "order-consumer"
          )
          .withClientId("product-service")
          .withCloseTimeout(30.seconds)
      )
    )

    val producer = (producerSettings ++ Blocking.any) >>> Producer.live.orDie
    val successProducer = (producer ++ settings) >>> SuccessProducer.live
    val failureProducer = (producer ++ settings) >>> FailureProducer.live
    val testProducerLayer =
      (producer ++ settings) >>> com.example.TestProducer.live

    val productService = ProductService.live

    val orderSubscription =
      settings.map(s => Has(Subscription.topics(s.get.topics.orderPlaced)))
    val orderConsumerLayer =
      (consumerSettings ++ productService ++ successProducer ++ failureProducer ++ orderSubscription) >>> OrderConsumer.live

    def isTest = args.headOption.fold(ZIO.succeed(false)){
      case "--test" => ZIO.succeed(true)
      case option => 
        zio.console.putStrErr(s"invalid option: '$option'") *> 
          ZIO.fail(RuntimeException(s"invalid option: '$option'"))
    }

    val app = (for {
      _ <- ZIO.ifM(isTest)(TestProducer.produceForever, ZIO.unit).fork
      _ <- OrderConsumer.consume
    } yield ())

    app
      .provideCustomLayer(testProducerLayer ++ orderConsumerLayer)
      .exitCode
}
