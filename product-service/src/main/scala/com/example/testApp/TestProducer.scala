package com.example

import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import org.apache.kafka.clients.producer.ProducerRecord
import com.example.messages.OrderPlaced
import zio.*
import zio.console.*

class TestProducer(producer: Producer, settings: Settings) {
  def produce(orderId: String) =
    val message = OrderPlaced(orderId, List.empty)
    val record =
      ProducerRecord(settings.topics.orderPlaced, orderId, message)
    producer
      .produce(record, Serde.string, OrderPlaced.serde)
      .map(x => x.offset)
}

object TestProducer {
  def live =
    ZLayer.fromServices[Producer, Settings, TestProducer](TestProducer(_, _))

  def produce(orderId: String) =
    for {
      _ <- putStrLn("producing...")
      offset <- ZIO.accessM[Has[TestProducer]](_.get.produce(orderId))
      _ <- putStrLn("produced")
    } yield offset
}
