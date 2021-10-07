package com.paymentService.repository

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "customerBankAccount")
data class CustomerBankAccount(
    val name: String,
    @Indexed(unique = true)
    val accountNumber: Int,
    val cvv: Int,
    var balance: Double
) {
    @Id
    lateinit var id: String
}
