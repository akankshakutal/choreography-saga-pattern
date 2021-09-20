package com.paymentService.kafka

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.kafka.annotation.KafkaListener

class SuccessEventProducer {
    @Bean
    fun topic() = NewTopic("payment-service-event", 5, 1)

    @KafkaListener(topics = ["payment-service-event"])
    fun listen(value: String?) {
        println(value)
    }
}