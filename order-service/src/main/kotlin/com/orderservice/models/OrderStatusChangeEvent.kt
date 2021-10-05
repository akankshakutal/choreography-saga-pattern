package com.orderservice.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderStatusChangeEvent(
        val orderId: String
)

data class UnknownEventException(override val message : String) : Exception(message)
