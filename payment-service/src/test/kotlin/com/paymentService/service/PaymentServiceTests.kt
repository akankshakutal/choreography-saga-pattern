package com.paymentService.service

import com.paymentService.kafka.PaymentServiceEventProducer
import com.paymentService.models.*
import com.paymentService.repository.*
import com.paymentService.repository.TransactionStatus.PENDING
import com.paymentService.repository.TransactionStatus.SUCCESS
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.EmptyResultDataAccessException

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
        every { save(any()) } returns Transaction("orderId", 1000.0, SUCCESS)
    }

    @Test
    internal fun `should return success response for pay`() {
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
                EmptyResultDataAccessException(1)
        val paymentDetails = PaymentDetails(accountNumber, name, cvv, "orderId")
        val paymentService = PaymentService(producer, customerBankAccountRepository, transactionsRepository)

        assertThrows<BankAccountNotFoundException> { paymentService.pay(paymentDetails) }
    }

    @Test
    internal fun `should throw exception when transactionsRepository does not have the account`() {
        every { transactionsRepository.findByOrderId(any()) } throws
                EmptyResultDataAccessException(1)
        val paymentDetails = PaymentDetails(accountNumber, name, cvv, "orderId")
        val paymentService = PaymentService(producer, customerBankAccountRepository, transactionsRepository)

        assertThrows<OrderNotFoundException> { paymentService.pay(paymentDetails) }
    }

    @Test
    internal fun `should return success response for add Account`() {
        val response = AccountCreationResponse(AccountStatus.CREATED)
        val accountDetails = AccountDetails("display name", 1234567890, 1000, 4500.0)
        val paymentService = PaymentService(producer, customerBankAccountRepository, transactionsRepository)

        val paymentResponse = paymentService.addAccount(accountDetails)

        paymentResponse shouldBe response
    }
}
