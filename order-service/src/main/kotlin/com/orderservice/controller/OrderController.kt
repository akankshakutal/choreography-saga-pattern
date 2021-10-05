package com.orderservice.controller

import com.orderservice.models.OrderRequest
import com.orderservice.models.OrderResponse
import com.orderservice.models.OrderTotalAmountResponse
import com.orderservice.service.OrderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import javax.validation.Valid

@RestController
@RequestMapping("/order")
class OrderController(
        @Autowired
        val orderService: OrderService
) {
    @PostMapping("/submit", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun submitOrder(@Valid @RequestBody orderRequest: OrderRequest): Mono<ResponseEntity<OrderResponse>> {
        return orderService.createOrder(orderRequest.toOrder())
                .map {
                    OrderResponse(it.orderId)
                }
                .map {
                    ResponseEntity.accepted().body(it)
                }
                .doOnSuccess {
                    println("Sending Successfull Response - $it")
                }
                .doOnError {
                    println("Some Error Occurred in creating the order.Message - [${it.message}]")
                }
    }

    @GetMapping("/totalAmount/{orderId}")
    fun getOrderTotalAmount(@PathVariable orderId: String): Mono<ResponseEntity<OrderTotalAmountResponse>> {
        return orderService.getOrderTotalAmount(orderId)
                .map {
                    ResponseEntity.ok().body(it)
                }
                .switchIfEmpty(ResponseEntity.notFound().build<OrderTotalAmountResponse>().toMono())
                .doOnSuccess {
                    println("Sending Successfull Response - $it")
                }
                .doOnError {
                    println("Some Error Occurred in getting totalAmount for the order.Message - [${it.message}]")
                }
    }
}