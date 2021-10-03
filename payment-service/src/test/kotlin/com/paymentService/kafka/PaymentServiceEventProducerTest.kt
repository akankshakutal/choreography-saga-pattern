package com.paymentService.kafka

import com.paymentService.models.PaymentEvent
import com.paymentService.models.PaymentFailedEvent
import com.paymentService.models.PaymentSucceedEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.ListenableFuture
import org.springframework.util.concurrent.SettableListenableFuture


internal class PaymentServiceEventProducerTest {
    private val kafkaTemplate = mockk<KafkaTemplate<Int, PaymentEvent>>()

    @Test
    internal fun `should produce PaymentSucceedEvent`() {
        val future =
            SettableListenableFuture<SendResult<String, PaymentEvent>>() as ListenableFuture<SendResult<Int, PaymentEvent>>
        every { kafkaTemplate.send(any(), any()) } returns future
        val paymentSucceedEvent = PaymentSucceedEvent("123456", "orderId")
        val eventProducer = PaymentServiceEventProducer(kafkaTemplate)

        eventProducer.produce("topic", paymentSucceedEvent)

        verify(exactly = 1) {
            kafkaTemplate.send("topic",paymentSucceedEvent)
        }
    }

    @Test
    internal fun `should produce PaymentFailedEvent`() {
        val future =
            SettableListenableFuture<SendResult<String, PaymentEvent>>() as ListenableFuture<SendResult<Int, PaymentEvent>>
        every { kafkaTemplate.send(any(), any()) } returns future
        val paymentSucceedEvent = PaymentFailedEvent("123456", "orderId")
        val eventProducer = PaymentServiceEventProducer(kafkaTemplate)

        eventProducer.produce("topic", paymentSucceedEvent)

        verify(exactly = 1) {
            kafkaTemplate.send("topic",paymentSucceedEvent)
        }
    }
}