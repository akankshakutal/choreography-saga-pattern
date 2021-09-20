package com.paymentService.models

data class PaymentSucceedEvent(val paymentId: String, val orderDetails: OrderDetails)

data class OrderDetails(
    val orderId: String,
    val productName: String,
    val ProductCount: Int,
    val totalPrice: Double,
    val address: Address
)

data class Address(
    val addressLine1: String,
    val addressLine2: String,
    val addressLine3: String,
    val landmark: String,
    val postalCode: String,
    val city: String,
    val state: String,
    val country: String,    
)

data class PaymentFailureEvent(val orderId: String)