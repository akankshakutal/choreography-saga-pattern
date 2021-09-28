package com.example

import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import org.apache.kafka.clients.producer.ProducerRecord
import com.example.messages.OrderPlaced
import zio.*
import zio.console.*
import zio.duration.*

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

  private def produce(orderId: String) =
    for {
      _ <- putStrLn("producing test message...")
      _ <- ZIO.accessM[Has[TestProducer]](_.get.produce(orderId))
      _ <- putStrLn("produced test message")
    } yield ()
  
  private def randomOrderId = zio.random.nextUUID.map(_.toString)
  private def produceRandom = randomOrderId.flatMap(TestProducer.produce)  

  def produceForever = produceRandom.repeat(Schedule.fixed(10.seconds))
}
