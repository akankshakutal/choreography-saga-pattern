package com.paymentService.service

import com.paymentService.kafka.PaymentServiceEventProducer
import com.paymentService.models.PaymentSucceedEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class PaymentService(@Autowired private val paymentServiceEventProducer: PaymentServiceEventProducer) {
    fun doPayment(): PaymentSucceedEvent {
        val paymentSucceedEvent = PaymentSucceedEvent("paymentId", "orderId")
        paymentServiceEventProducer.produce(paymentSucceedEvent, Random(10000).nextInt())
        return paymentSucceedEvent
    }
}