package com.orderservice.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal

@Document("com/orders")
data class Order(
        @Id
        @Indexed(unique = true)
        val orderId :String,
        val customerId: String,
        val items: List<Item>,
        val totalAmount  : BigDecimal,
        val shippingAddress : ShippingAddress,
        val status : OrderStatus
)
