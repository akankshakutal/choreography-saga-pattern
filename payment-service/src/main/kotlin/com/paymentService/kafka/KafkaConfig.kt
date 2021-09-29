package com.paymentService.kafka

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaConfig {
    companion object {
        @Value("spring.kafka.consumer.productService.topic")
        const val productServiceTopicName: String = "ProductAvailed"
    }
}