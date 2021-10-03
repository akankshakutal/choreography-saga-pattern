package com.paymentService.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.payment.paymentService.utils.any
import com.paymentService.models.PaymentDetails
import com.paymentService.service.PaymentResponse
import com.paymentService.service.PaymentService
import com.paymentService.service.PaymentStatus
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(PaymentController::class)
class PaymentControllerTest(@Autowired private val mockMvc: MockMvc) {

    @MockBean
    private lateinit var paymentService: PaymentService

    @Test
    fun `should save order details and return it with 200 ok`() {
        val response = PaymentResponse(PaymentStatus.SUCCESS, 1000.0)
        Mockito.`when`(paymentService.pay(any())).thenReturn(response)
        val paymentDetails = PaymentDetails(1234567890, "display name", 1000, "orderId")

        val requestBuilder = MockMvcRequestBuilders
            .post("/make/payment")
            .content(ObjectMapper().writeValueAsString(paymentDetails))
            .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUCCESS"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(1000))
            .andReturn()

    }
}