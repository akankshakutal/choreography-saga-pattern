package com.paymentService.repository

import io.kotlintest.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.ActiveProfiles

@Disabled
@ActiveProfiles("test")
@DataMongoTest
class CustomerBankAccountRepositoryTest {
    @Autowired
    lateinit var customerBankAccountRepository: CustomerBankAccountRepository

    @AfterEach
    internal fun tearDown() {
        customerBankAccountRepository.deleteAll()
    }

    @Test
    internal fun `should return CustomerBankAccount`() {
        val accountNumber = 1234567890
        val cvv = 543
        val customerBankAccount = CustomerBankAccount("display name", accountNumber, cvv, 2400.0)
            .apply { id = "123456" }

        customerBankAccountRepository.save(customerBankAccount)

        val actual = customerBankAccountRepository.findByAccountNumberAndCvv(accountNumber, cvv)

        actual shouldBe customerBankAccount
    }
}