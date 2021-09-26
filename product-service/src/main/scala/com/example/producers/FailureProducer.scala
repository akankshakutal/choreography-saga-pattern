package com.example
package producers

import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import org.apache.kafka.clients.producer.ProducerRecord
import com.example.messages.ProductAvailFailed
import zio.*

class FailureProducer(producer: Producer, settings: Settings) {
  def produce(orderId: String) =
    val message = ProductAvailFailed(orderId)
    val record =
      ProducerRecord(settings.topics.productAvailFailed, orderId, message)
    producer
      .produce(record, Serde.string, ProductAvailFailed.serde)
      .map(x => x.offset)
}

object FailureProducer {
  def live = ZLayer.fromServices[Producer, Settings, FailureProducer](FailureProducer(_,_))

  def produce(orderId:String) = 
    ZIO.accessM[Has[FailureProducer]](_.get.produce(orderId))
}