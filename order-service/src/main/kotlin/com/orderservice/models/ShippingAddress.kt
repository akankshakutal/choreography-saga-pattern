package com.orderservice.models

import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class ShippingAddress(
        @NotEmpty
        @field:Size(min = 1, max = 500)
        val addressLineOne: String,
        @field:Size(max = 100)
        val addressLineTwo: String = "",
        @field:Size(max = 50)
        val addressLineThree: String = "",
        @field:NotEmpty
        val city: String,
        @field:NotNull
        @field:Min(0)
        val pincode: Int,
        @field:NotEmpty
        val country: String,
        val type: AddressType
)