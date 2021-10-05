package com.orderservice.models

import java.math.BigDecimal

data class Item(
        val itemId : String,
        val quantity :Int,
        val pricePerUnit : BigDecimal
)