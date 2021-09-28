package com.paymentService.controller

import com.paymentService.models.PaymentSucceedEvent
import com.paymentService.service.PaymentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PaymentController(@Autowired val paymentService: PaymentService) {

    @PostMapping("/make/payment")
    fun makePayment(): PaymentSucceedEvent {
        return paymentService.doPayment()
    }
}