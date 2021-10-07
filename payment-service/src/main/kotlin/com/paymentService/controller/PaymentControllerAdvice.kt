package com.paymentService.controller

import com.paymentService.models.PaymentErrorResponse
import com.paymentService.service.BankAccountNotFoundException
import com.paymentService.service.OrderNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class PaymentControllerAdvice {

    @ExceptionHandler(Exception::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    fun globalExceptionHandler(exception: Throwable): PaymentErrorResponse {
        return PaymentErrorResponse("ERR-1", "Internal server error")
    }

    @ExceptionHandler(BankAccountNotFoundException::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleBankAccountNotFoundException(exception: BankAccountNotFoundException): PaymentErrorResponse {
        return PaymentErrorResponse("ERR-2", "Bank Account Details not found")
    }

    @ExceptionHandler(OrderNotFoundException::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleOrderNotFoundException(exception: BankAccountNotFoundException): PaymentErrorResponse {
        return PaymentErrorResponse("ERR-2", "Bank Account Details not found")
    }
}