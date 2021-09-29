package com.paymentService.models

data class PaymentDetails(val accountNumber: String, val name: String, val amount: Int, val orderId: String)