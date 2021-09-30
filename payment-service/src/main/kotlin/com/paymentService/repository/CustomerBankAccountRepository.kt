package com.paymentService.repository

import org.springframework.data.mongodb.repository.MongoRepository

interface CustomerBankAccountRepository : MongoRepository<CustomerBankAccount, String> {
    fun findByAccountNumberAndCvv(accountNumber: Int, cvv: Int): CustomerBankAccount
}