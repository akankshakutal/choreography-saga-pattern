package com.paymentService.models


data class PaymentResponse(val status: PaymentStatus, val amount: Double)

data class PaymentErrorResponse(val errorCode: String, val message: String)

enum class PaymentStatus { SUCCESS, FAILED }