package com.paymentService.models

sealed class PaymentEvent(open val paymentId: String, open val orderId: String)

data class PaymentSucceedEvent(override val paymentId: String, override val orderId: String) :
    PaymentEvent(paymentId, orderId)

data class PaymentFailedEvent(override val paymentId: String, override val orderId: String) :
    PaymentEvent(paymentId, orderId)