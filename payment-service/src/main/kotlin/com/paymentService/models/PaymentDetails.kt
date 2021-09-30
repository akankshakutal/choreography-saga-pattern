package com.paymentService.models

data class PaymentDetails(val accountNumber: Int, val name: String, val cvv: Int, val orderId: String)