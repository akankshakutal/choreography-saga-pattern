package com.example
package producers

import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import org.apache.kafka.clients.producer.ProducerRecord
import com.example.messages.ProductAvailed
import zio.*

class SuccessProducer(producer: Producer, settings: Settings) {
  def produce(orderId: String) =
    val message = ProductAvailed(orderId)
    val record =
      ProducerRecord(settings.topics.productAvailed, orderId, message)
    producer
      .produce(record, Serde.string, ProductAvailed.serde)
      .map(x => x.offset)
}

object SuccessProducer {
  def live = ZLayer.fromServices[Producer, Settings, SuccessProducer](SuccessProducer(_,_))

  def produce(orderId:String) = 
    ZIO.accessM[Has[SuccessProducer]](_.get.produce(orderId))
}