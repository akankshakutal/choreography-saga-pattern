package com.paymentService.kafka

import com.paymentService.models.PaymentSucceedEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class PaymentServiceEventProducer(private val template: KafkaTemplate<Int, PaymentSucceedEvent>) {

    @Value("spring.kafka.producer.paymentSucceed.topic")
    lateinit var paymentSucceedTopicName: String

    fun produce(message: PaymentSucceedEvent, key: Int) {
        template.send(paymentSucceedTopicName, key, message)
    }
}

