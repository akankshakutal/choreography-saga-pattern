package com.paymentService.kafka

import com.paymentService.models.PaymentEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class PaymentServiceEventProducer(private val template: KafkaTemplate<Int, PaymentEvent>) {

    fun produce(topicName: String, message: PaymentEvent, key: Int) {
        template.send(topicName, key, message)
    }
}

