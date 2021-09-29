package com.paymentService.service

import com.paymentService.kafka.PaymentServiceEventProducer
import com.paymentService.models.PaymentDetails
import com.paymentService.models.PaymentSucceedEvent
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
class PaymentServiceTests {

    private val producer = mockk<PaymentServiceEventProducer>()

    @Test
    internal fun `should produce event`() {
        every { producer.produce(any(), any(),any()) } returns Unit
        val response = PaymentResponse("SUCCESS", 1000)
        val paymentSucceedEvent = PaymentSucceedEvent("paymentId", "orderId")
        val paymentDetails = PaymentDetails("KOTAK1234", "display name", 1000, "orderId")
        val paymentService = PaymentService(producer)

        val paymentResponse = paymentService.pay(paymentDetails)

        paymentResponse shouldBe response
        verify(exactly = 1) {
            producer.produce("",paymentSucceedEvent,any())
        }
    }
}
