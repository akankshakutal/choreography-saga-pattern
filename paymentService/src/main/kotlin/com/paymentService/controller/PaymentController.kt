package com.paymentService.controller

import com.paymentService.models.PaymentDetails
import com.paymentService.service.PaymentResponse
import com.paymentService.service.PaymentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PaymentController(val paymentService: PaymentService) {

    @PostMapping("/make/payment")
    fun makePayment(@RequestBody paymentDetails: PaymentDetails): PaymentResponse {
        return paymentService.pay(paymentDetails)
    }
}