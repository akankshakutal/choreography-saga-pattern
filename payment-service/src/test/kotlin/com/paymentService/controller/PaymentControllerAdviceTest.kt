package com.paymentService.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.payment.paymentService.utils.any
import com.paymentService.models.*
import com.paymentService.service.BankAccountNotFoundException
import com.paymentService.service.OrderNotFoundException
import com.paymentService.service.PaymentService
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(PaymentController::class)
class PaymentControllerAdviceTest(@Autowired private val mockMvc: MockMvc) {

    @MockBean
    private lateinit var paymentService: PaymentService

    @Test
    fun `should return error response for make payment API`() {
        Mockito.`when`(paymentService.pay(any())).thenThrow(RuntimeException("Internal server error"))
        val paymentDetails = PaymentDetails(1234567890, "display name", 1000, "orderId")

        val requestBuilder = MockMvcRequestBuilders
            .post("/make/payment")
            .content(ObjectMapper().writeValueAsString(paymentDetails))
            .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(requestBuilder)
            .andExpect(MockMvcResultMatchers.status().is5xxServerError)
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("ERR-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Internal server error"))
            .andReturn()

    }

    @Test
    fun `should return error response for add account API`() {
        val accountDetails = AccountDetails("display name", 1234567890, 1000, 4500.0)
        Mockito.`when`(paymentService.addAccount(any())).thenThrow(RuntimeException("Internal server error"))

        val requestBuilder = MockMvcRequestBuilders
            .post("/add/account")
            .content(ObjectMapper().writeValueAsString(accountDetails))
            .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(requestBuilder)
            .andExpect(MockMvcResultMatchers.status().is5xxServerError)
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("ERR-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Internal server error"))
            .andReturn()
    }

    @Test
    @Disabled
    fun `should return error response for account not found`() {
        Mockito.`when`(paymentService.pay(any())).thenThrow(BankAccountNotFoundException())
        val paymentDetails = PaymentDetails(1234567890, "display name", 1000, "orderId")

        val requestBuilder = MockMvcRequestBuilders
            .post("/make/payment")
            .content(ObjectMapper().writeValueAsString(paymentDetails))
            .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(requestBuilder)
            .andExpect(MockMvcResultMatchers.status().is5xxServerError)
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("ERR-2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Bank Account Details not found"))
            .andReturn()

    }

    @Test
    @Disabled
    fun `should return error response for transaction not found`() {
        Mockito.`when`(paymentService.pay(any())).thenThrow(OrderNotFoundException())
        val paymentDetails = PaymentDetails(1234567890, "display name", 1000, "orderId")

        val requestBuilder = MockMvcRequestBuilders
            .post("/make/payment")
            .content(ObjectMapper().writeValueAsString(paymentDetails))
            .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(requestBuilder)
            .andExpect(MockMvcResultMatchers.status().is5xxServerError)
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("ERR-2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Bank Account Details not found"))
            .andReturn()
    }

}