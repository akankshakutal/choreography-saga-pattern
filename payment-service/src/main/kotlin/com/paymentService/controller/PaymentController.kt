package com.paymentService.controller

import com.paymentService.models.AccountDetails
import com.paymentService.models.PaymentDetails
import com.paymentService.service.PaymentService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PaymentController(val paymentService: PaymentService) {

    @PostMapping("/make/payment")
    fun makePayment(@RequestBody paymentDetails: PaymentDetails) = paymentService.pay(paymentDetails)

    @PostMapping("/add/account")
    fun createAccount(@RequestBody accountDetails: AccountDetails) = paymentService.addAccount(accountDetails)
}