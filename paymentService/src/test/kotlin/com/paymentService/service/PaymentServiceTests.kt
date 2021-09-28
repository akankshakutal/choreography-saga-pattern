package com.paymentService.service

import com.paymentService.kafka.PaymentServiceEventProducer
import com.paymentService.models.PaymentDetails
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PaymentServiceTests {

    private val producer = mockk<PaymentServiceEventProducer>()

    @Test
    internal fun `should produce event`() {
        every { producer.produce(any(), any()) } returns Unit
        val response = PaymentResponse("SUCCESS", 1000)
        val paymentDetails = PaymentDetails("KOTAK1234", "display name", 1000, "orderId")
        val paymentService = PaymentService(producer)

        val paymentResponse = paymentService.pay(paymentDetails)

        paymentResponse shouldBe response
    }
}
