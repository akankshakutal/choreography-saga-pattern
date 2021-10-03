package com.paymentService.service

import com.paymentService.kafka.PaymentServiceEventProducer
import com.paymentService.models.PaymentDetails
import com.paymentService.models.PaymentFailedEvent
import com.paymentService.models.PaymentSucceedEvent
import com.paymentService.repository.CustomerBankAccount
import com.paymentService.repository.CustomerBankAccountRepository
import com.paymentService.repository.Transaction
import com.paymentService.repository.TransactionStatus.PENDING
import com.paymentService.repository.TransactionsRepository
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PaymentServiceTests {
    private val accountNumber = 1234567890
    private val cvv = 543
    private val name = "display name"
    private val customerBankAccount = CustomerBankAccount(name, accountNumber, cvv, 2400.0).apply { id = "123456" }
    private val producer = mockk<PaymentServiceEventProducer> {
        every { produce(any(), any()) } returns Unit
    }
    private val customerBankAccountRepository = mockk<CustomerBankAccountRepository> {
        every { findByAccountNumberAndCvv(any(), any()) } returns customerBankAccount
        every { save(any()) } returns customerBankAccount.copy(balance = 23000.0)
    }
    private val transactionsRepository = mockk<TransactionsRepository> {
        every { findByOrderId(any()) } returns Transaction("orderId", 1000.0, PENDING)
    }

    @Test
    internal fun `should return success response`() {
        val response = PaymentResponse(PaymentStatus.SUCCESS, 1000.0)
        val paymentSucceedEvent = PaymentSucceedEvent("123456", "orderId")
        val paymentDetails = PaymentDetails(accountNumber, name, cvv, "orderId")
        val paymentService = PaymentService(producer, customerBankAccountRepository, transactionsRepository)

        val paymentResponse = paymentService.pay(paymentDetails)

        paymentResponse shouldBe response
        verify(exactly = 1) { producer.produce("PaymentSucceed", paymentSucceedEvent) }
    }

    @Test
    internal fun `should return failure response`() {
        every { transactionsRepository.findByOrderId(any()) } returns Transaction("orderId", 10000.0, PENDING)
        val response = PaymentResponse(PaymentStatus.FAILED, 10000.0)
        val paymentFailedEvent = PaymentFailedEvent("123456", "orderId")
        val paymentDetails = PaymentDetails(accountNumber, name, cvv, "orderId")
        val paymentService = PaymentService(producer, customerBankAccountRepository, transactionsRepository)

        val paymentResponse = paymentService.pay(paymentDetails)

        paymentResponse shouldBe response
        verify(exactly = 1) { producer.produce("PaymentFailed", paymentFailedEvent) }
    }

    @Test
    internal fun `should throw exception when customerBankAccountRepository does not have the account`() {
        every { customerBankAccountRepository.findByAccountNumberAndCvv(any(), any()) } throws
                RuntimeException("Data not Found")
        val paymentDetails = PaymentDetails(accountNumber, name, cvv, "orderId")
        val paymentService = PaymentService(producer, customerBankAccountRepository, transactionsRepository)

        assertThrows<RuntimeException> { paymentService.pay(paymentDetails) }
    }
}
