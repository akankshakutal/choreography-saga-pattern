package com.example

import zio.kafka.consumer.Consumer
import zio.json.*
import zio.kafka.serde.Serde
import zio.kafka.consumer.Subscription
import zio.stream.ZStream
import com.example.messages.OrderPlaced
import scala.util.Success
import scala.util.Failure
import com.example.models.OrderItem
import zio.clock.Clock
import com.example.consumers.AppConsumer
import zio.console.*
import zio.console.Console
import com.example.producers.SuccessProducer
import com.example.producers.FailureProducer
import zio.kafka.consumer.ConsumerSettings
import zio.*
import zio.blocking.Blocking

object OrderConsumer {
  val live = ZLayer.fromServices[
    ConsumerSettings,
    Subscription,
    ProductService,
    SuccessProducer,
    FailureProducer,
    OrderConsumer
  ](OrderConsumer(_,_,_,_,_))

  def consume: ZIO[Has[OrderConsumer] & Has[Console.Service] & Has[Blocking.Service] & Has[Clock.Service], Nothing, Unit] = 
    ZIO.access[Has[OrderConsumer]](x => x.get.consume).flatten
}

class OrderConsumer(
    consumerSettings: ConsumerSettings,
    subscription: Subscription,
    productService: ProductService,
    successProducer: SuccessProducer,
    failureProducer: FailureProducer
) {
  def consume: URIO[Console & Blocking & Clock, Unit] = Consumer.consumeWith(
    consumerSettings,
    subscription,
    Serde.string,
    OrderPlaced.serde.asTry
  ) {
    case (key, Success(orderPlaced)) =>
      val consumer = for {
        _ <- putStrLn(s"order recieved: \n${orderPlaced.toJson}").catchAll(_ =>
          ZIO.unit
        )
        _ <- productService
          .avail(orderPlaced.items.map(i => OrderItem(i.productId, i.quantity)))
          .tapError(x => putStrLnErr(x.getMessage))
          .orElse(failureProducer.produce(orderPlaced.orderId))
          .orDie
        _ <- successProducer
          .produce(orderPlaced.orderId)
          .tapError(e => putStrErr(e.getMessage))
          .orDie
      } yield ()
      consumer
    case (key, Failure(err)) =>
      putStrErr(s"serde error: ${err.getMessage}")
        .catchAll(_ => ZIO.unit)
  }.orDie
}
