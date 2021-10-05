package com.orderservice.models

import java.math.BigDecimal
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty

data class OrderRequest(
        @field:NotEmpty
        val customerId: String,
        @field:NotEmpty
        val items: List<Item>,
        @field:Min(1)
        val totalAmount: BigDecimal,
        @field:Valid
        val shippingAddress: ShippingAddress
) {
    fun toOrder(): Order {
        return Order(
                orderId = UUID.randomUUID().toString(),
                customerId = customerId,
                items = items,
                totalAmount = totalAmount,
                shippingAddress = shippingAddress,
                status = OrderStatus.RECEIVED
        )
    }
}
