package com.paymentService.kafka

import com.paymentService.models.ProductAvailed
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ProductAvailedEventConsumer {
    @Bean
    fun topic() = NewTopic(KafkaConfig.productServiceTopicName, 5, 1)

    @KafkaListener(topics = [KafkaConfig.productServiceTopicName])
    fun listen(productAvailedEvent: ProductAvailed) {
        println(productAvailedEvent)
    }
}