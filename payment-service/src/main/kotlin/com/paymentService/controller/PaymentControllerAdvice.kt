package com.paymentService.controller

import com.paymentService.models.PaymentErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class PaymentControllerAdvice {

    @ExceptionHandler(Exception::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleBadRequestException(): PaymentErrorResponse {
        return PaymentErrorResponse("ERR-1", "Internal server error")
    }
}