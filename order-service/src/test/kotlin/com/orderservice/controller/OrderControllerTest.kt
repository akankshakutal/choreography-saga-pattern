package com.orderservice.controller

import com.orderservice.models.AddressType
import com.orderservice.models.Item
import com.orderservice.models.Order
import com.orderservice.models.OrderRequest
import com.orderservice.models.OrderShippingAddressResponse
import com.orderservice.models.OrderStatus
import com.orderservice.models.OrderTotalAmountResponse
import com.orderservice.models.ShippingAddress
import com.orderservice.service.OrderService
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.util.*

@ExtendWith(MockitoExtension::class)
@WebFluxTest(controllers = [OrderController::class])
internal class OrderControllerTest {
    @Autowired
    lateinit var webTestClient: WebTestClient

    @MockBean
    lateinit var orderService: OrderService

    private val item = Item(
            itemId = "Item-id",
            quantity = 1,
            pricePerUnit = 100.toBigDecimal()
    )

    private val shippingAddress = ShippingAddress(
            addressLineOne = "address-line-one",
            city = "city",
            pincode = 123456789,
            country = "India",
            type = AddressType.HOME
    )

    private val orderRequest = OrderRequest(
            customerId = "customer-id",
            items = listOf(item),
            totalAmount = 1234.toBigDecimal(),
            shippingAddress = shippingAddress
    )

    private val order = Order(
            orderId = "8d8b30e3-de52-4f1c-a71c-9905a8043dac",
            customerId = orderRequest.customerId,
            items = orderRequest.items,
            totalAmount = orderRequest.totalAmount,
            shippingAddress = orderRequest.shippingAddress,
            status = OrderStatus.RECEIVED
    )

    private val defaultUUID = UUID.fromString("8d8b30e3-de52-4f1c-a71c-9905a8043dac")

    @BeforeEach
    fun setUp() {
        Mockito.`when`(orderService.createOrder(order)).thenReturn(Mono.just(order))

        mockkStatic(UUID::class)
        every {
            UUID.randomUUID()
        } returns defaultUUID
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(UUID::class)
    }

    @Test
    fun `should create a new order`() {
        webTestClient
                .post()
                .uri("http://localhost:8080/order/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus()
                .isAccepted
                .expectBody()
                .jsonPath("orderId").isEqualTo("8d8b30e3-de52-4f1c-a71c-9905a8043dac")
    }

    @Test
    fun `should return the totalAmount for the order`() {
        val orderTotalAmountResponse = OrderTotalAmountResponse(order.totalAmount)
        Mockito.`when`(orderService.getOrderTotalAmount(order.orderId)).thenReturn(Mono.just(orderTotalAmountResponse))
        webTestClient
                .get()
                .uri("http://localhost:8080/order/totalAmount/8d8b30e3-de52-4f1c-a71c-9905a8043dac")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("totalAmount").isEqualTo("1234")
    }

    @Test
    fun `should return 404 HTTP status when order not found for totalAmount request`() {
        Mockito.`when`(orderService.getOrderTotalAmount(order.orderId)).thenReturn(Mono.empty())
        webTestClient
                .get()
                .uri("http://localhost:8080/order/totalAmount/8d8b30e3-de52-4f1c-a71c-9905a8043dac")
                .exchange()
                .expectStatus()
                .isNotFound
    }

    @Test
    fun `should return the shippingAddress for the order`() {
        val orderShippingAddressResponse = OrderShippingAddressResponse(order.shippingAddress)
        Mockito.`when`(orderService.getOrderShippingAddress(order.orderId)).thenReturn(Mono.just(orderShippingAddressResponse))
        webTestClient
                .get()
                .uri("http://localhost:8080/order/shippingAddress/8d8b30e3-de52-4f1c-a71c-9905a8043dac")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(OrderShippingAddressResponse::class.java)
    }

    @Test
    fun `should return 404 HTTP status when order not found for shippingAddress request`() {
        Mockito.`when`(orderService.getOrderShippingAddress(order.orderId)).thenReturn(Mono.empty())
        webTestClient
                .get()
                .uri("http://localhost:8080/order/shippingAddress/8d8b30e3-de52-4f1c-a71c-9905a8043dac")
                .exchange()
                .expectStatus()
                .isNotFound
    }
}