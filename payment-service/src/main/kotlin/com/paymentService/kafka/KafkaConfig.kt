package com.paymentService.kafka

import org.springframework.beans.factory.annotation.Value

object KafkaConfig {
    @Value("spring.kafka.consumer.productService.topic")
    const val productServiceTopicName: String = "ProductAvailed"

    @Value("spring.kafka.consumer.shipment.topic")
    const val shipmentServiceTopicName: String = "ShipmentFailed"

    @Value("spring.kafka.producer.paymentSucceed.topic")
    const val paymentSucceedTopicName: String = "PaymentSucceed"

    @Value("spring.kafka.producer.paymentFailed.topic")
    const val paymentFailedTopicName: String = "PaymentFailed"
}