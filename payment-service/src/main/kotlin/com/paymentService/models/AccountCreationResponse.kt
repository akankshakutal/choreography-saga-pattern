package com.paymentService.models

data class AccountCreationResponse(val status: AccountStatus)

enum class AccountStatus {
    CREATED,
    FAILED
}
