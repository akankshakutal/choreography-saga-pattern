package com.paymentService.models

data class PaymentSucceedEvent(val paymentId: String, val orderId: String)