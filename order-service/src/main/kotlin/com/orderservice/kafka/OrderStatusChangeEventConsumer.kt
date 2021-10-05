package com.orderservice.kafka

import com.orderservice.models.OrderStatusChangeEvent
import com.orderservice.service.OrderService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class OrderStatusChangeEventConsumer(
        @Value("\${kafka.consumer.topic.names}")
        val topics: List<String>,
        @Autowired
        val orderService: OrderService
) {
    @KafkaListener(topics = ["#{__listener.topics}"], containerFactory = "kafkaListenerContainerFactory")
    fun listener(consumerRecord: ConsumerRecord<String, OrderStatusChangeEvent>) {
        Mono.defer {
            orderService.updateStatusInDB(consumerRecord.value(), consumerRecord.topic())
        }
                .subscribe()
    }
}