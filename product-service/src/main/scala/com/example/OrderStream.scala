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

class OrderStream(
    productService: ProductService,
    settings: Settings,
    clock: Clock.Service,
    consumer: Consumer,
    console: zio.console.Console.Service,
) {
  private def sendFailureMessage(orderId: String): ZIO[Any, IOException, Unit] =
    ???
  def stream() =
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
              .avail(order.items.map(i => OrderItem(i.productId, i.quantity)))
              .tapError(x => console.putStrLnErr(x))
              //todo : when availaing fails,
              //raise a failure event and move on
              //if that also fails, break the stream and let it restart by infra
              .catchAll(_ => sendFailureMessage(order.orderId))
              .as(offset)
        }
      }
      .aggregateAsync(Consumer.offsetBatches)
      .provideLayer(ZLayer.succeed(clock))
}

object OrderStream {
  val live = ZStream.accessStream {(service:Has[OrderStream]) =>
    service.get.stream()
  }
  .run(ZSink.foreach(_.commit))
}
