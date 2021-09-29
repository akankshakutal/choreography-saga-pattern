package com.paymentService.service

import com.paymentService.kafka.PaymentServiceEventProducer
import com.paymentService.models.PaymentDetails
import com.paymentService.models.PaymentFailedEvent
import com.paymentService.models.PaymentSucceedEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class PaymentService(@Autowired private val paymentServiceEventProducer: PaymentServiceEventProducer) {

    @Value("spring.kafka.producer.paymentSucceed.topic")
    lateinit var paymentSucceedTopicName: String

    @Value("spring.kafka.producer.paymentFailed.topic")
    lateinit var paymentFailedTopicName: String

    fun pay(paymentDetails: PaymentDetails): PaymentResponse {
        val key = Random(10000).nextInt()
        if (paymentDetails.amount < 100) {
            val paymentSucceedEvent = PaymentSucceedEvent("paymentId", "orderId")
            paymentServiceEventProducer.produce(paymentSucceedTopicName, paymentSucceedEvent, key)
        } else {
            val paymentSucceedEvent = PaymentFailedEvent("paymentId", "orderId")
            paymentServiceEventProducer.produce(paymentFailedTopicName, paymentSucceedEvent, key)
        }
        return PaymentResponse("SUCCESS", paymentDetails.amount)
    }
}

data class PaymentResponse(val status: String, val amount: Int)