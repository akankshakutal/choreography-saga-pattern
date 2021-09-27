package com.example

import zio.kafka.consumer.Consumer
import zio.json.*
import zio.kafka.serde.Serde
import zio.kafka.consumer.Subscription
import zio.stream.ZStream
import com.example.messages.OrderPlaced
import scala.util.Try
import zio.kafka.consumer.CommittableRecord
import scala.util.Success
import scala.util.Failure
import zio.kafka.consumer.Offset
import zio.console.Console.Service
import com.example.models.OrderItem
import zio.stream.ZSink
import java.io.IOException
import zio.clock.Clock
import com.example.consumers.AppConsumer
import zio.ZIO
import zio.Has
import zio.ZLayer
import zio.console.*
import zio.console.Console
import com.example.producers.SuccessProducer
import com.example.producers.FailureProducer

class OrderStream(
    productService: ProductService,
    settings: Settings,
    successProducer: SuccessProducer,
    failureProducer: FailureProducer,
    clock: Clock.Service,
    consumer: Consumer,
    console: Console.Service
) {
  def stream =
    consumer
      .subscribeAnd(Subscription.topics(settings.topics.orderPlaced))
      .plainStream(Serde.string, OrderPlaced.serde.asTry)
      .mapM { case CommittableRecord(rec, offset) =>
        rec.value match {
          case Failure(err) =>
            //logging error when serialisation has failed.
            //todo: there is no notification or retry mechanism
            //these messages will be lost
            console.putStrErr(err.getMessage).as(offset)
          case Success(order) =>
            productService
              //avail the product
              .avail(order.items.map(i => OrderItem(i.productId, i.quantity)))
              // broadcast event
              .tap(_ =>
                successProducer.produce(order.orderId).mapError(_.getMessage)
              )
              //when availaing fails,
              //raise a failure event and move on
              //if that also fails, break the stream and let it restart by infra
              .tapError(x => console.putStrLnErr(x))
              .catchAll(_ => failureProducer.produce(order.orderId).orDie)
              .as(offset)
        }
      }
      .aggregateAsync(Consumer.offsetBatches)
      .provideLayer(ZLayer.succeed(clock))
      .run(ZSink.foreach(_.commit))
}

object OrderStream {
  val live: ZLayer[Has[
    ProductService
  ] & Has[Settings] & Has[Consumer] & Has[Clock.Service] & Has[SuccessProducer] & Has[FailureProducer] & Has[zio.console.Console.Service], Nothing, Has[
    OrderStream
  ]] = ZLayer.fromServices[
    ProductService,
    Settings,
    SuccessProducer,
    FailureProducer,
    Clock.Service,
    Consumer,
    zio.console.Console.Service,
    OrderStream
  ](OrderStream(_, _, _, _, _, _, _))

  def stream = 
    putStrLn("order stream started...") *>
      ZIO.accessM[Has[OrderStream]](s => s.get.stream)
}
