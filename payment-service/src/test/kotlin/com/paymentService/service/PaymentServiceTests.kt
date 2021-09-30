package com.paymentService.service

import com.paymentService.kafka.PaymentServiceEventProducer
import com.paymentService.models.PaymentDetails
import com.paymentService.models.PaymentSucceedEvent
import com.paymentService.repository.*
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
    private val accountNumber = 1234567890
    private val cvv = 543
    private val name = "display name"
    private val producer = mockk<PaymentServiceEventProducer>()
    private val customerBankAccount = CustomerBankAccount(name, accountNumber, cvv, 24000.0).apply { id = "123456" }
    private val customerBankAccountRepository = mockk<CustomerBankAccountRepository> {
        every { findByAccountNumberAndCvv(any(), any()) } returns customerBankAccount
        every { save(any()) } returns customerBankAccount.copy(balance = 23000.0)
    }
    private val transactionsRepository = mockk<TransactionsRepository> {
        every { findByOrderId(any()) } returns Transaction("orderId", 1000.0, TransactionStatus.PENDING)
    }

    @Test
    internal fun `should produce event`() {
        every { producer.produce(any(), any()) } returns Unit
        val response = PaymentResponse("SUCCESS", 1000.0)
        val paymentSucceedEvent = PaymentSucceedEvent("123456", "orderId")
        val paymentDetails = PaymentDetails(accountNumber, name, cvv, "orderId")
        val paymentService = PaymentService(producer, customerBankAccountRepository, transactionsRepository)

        val paymentResponse = paymentService.pay(paymentDetails)

        paymentResponse shouldBe response
        verify(exactly = 1) { producer.produce("", paymentSucceedEvent) }
    }
}
