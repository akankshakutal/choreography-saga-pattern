package com.paymentService.repository

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "transaction")
data class Transaction(val orderId: String, val amount: Double, var status: TransactionStatus) {
    @Id
    lateinit var id: String

    var accountNumber: Int? = null
}

enum class TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED,
    ROLLBACK
}