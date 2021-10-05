package com.orderservice.service

import com.orderservice.models.AddressType
import com.orderservice.models.Item
import com.orderservice.models.Order
import com.orderservice.models.OrderRequest
import com.orderservice.models.OrderStatus
import com.orderservice.models.OrderStatusChangeEvent
import com.orderservice.models.OrderTotalAmountResponse
import com.orderservice.models.ShippingAddress
import com.orderservice.repository.OrderRepository
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.concurrent.ListenableFuture
import reactor.test.StepVerifier
import java.util.*

@ExtendWith(SpringExtension::class)
@DataMongoTest
internal class OrderServiceTest {
    @Autowired
    lateinit var orderRepository: OrderRepository

    @Mock
    lateinit var kafkaTemplate: KafkaTemplate<String, Order>

    @Mock
    lateinit var listenableFuture: ListenableFuture<SendResult<String, Order>>

    @Mock
    lateinit var sendResult: SendResult<String, Order>

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
        orderRepository.deleteAll()
        Mockito.`when`(kafkaTemplate.send("OrderPlaced", order.orderId, order)).thenReturn(listenableFuture)
        Mockito.`when`(listenableFuture.get()).thenReturn(sendResult)
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
    fun `should save order in DB`() {
        val mono = OrderService(orderRepository, kafkaTemplate, "OrderPlaced").createOrder(order)
        StepVerifier.create(mono)
                .expectSubscription()
                .consumeNextWith {
                    it shouldBe order
                }
                .verifyComplete()
    }

    @Test
    fun `should return totalAmount for order`() {
        orderRepository.save(order).block()
        val mono = OrderService(orderRepository, kafkaTemplate, "OrderPlaced").getOrderTotalAmount(order.orderId)
        StepVerifier.create(mono)
                .expectSubscription()
                .consumeNextWith {
                    it shouldBe OrderTotalAmountResponse(order.totalAmount)
                }
                .verifyComplete()
    }

    @Test
    fun `should update status in DB when event received from ProductAvailed topic`() {
        orderRepository.save(order).block()
        val orderService = OrderService(orderRepository, kafkaTemplate, "OrderPlaced")
        val updateInDb = orderService.updateStatusInDB(OrderStatusChangeEvent("8d8b30e3-de52-4f1c-a71c-9905a8043dac"), "ProductAvailed")

        StepVerifier.create(updateInDb)
                .expectSubscription()
                .consumeNextWith {
                    it shouldBe true
                }
                .verifyComplete()
        val updatedOrder = orderRepository.findById(order.orderId).block()!!
        updatedOrder.status shouldBe OrderStatus.IN_PROGRESS
    }

    @Test
    fun `should update status in DB when event received from ProductAvailFailed topic`() {
        orderRepository.save(order).block()
        val orderService = OrderService(orderRepository, kafkaTemplate, "OrderPlaced")
        val updateInDb = orderService.updateStatusInDB(OrderStatusChangeEvent("8d8b30e3-de52-4f1c-a71c-9905a8043dac"), "ProductAvailFailed")

        StepVerifier.create(updateInDb)
                .expectSubscription()
                .consumeNextWith {
                    it shouldBe true
                }
                .verifyComplete()
        val updatedOrder = orderRepository.findById(order.orderId).block()!!
        updatedOrder.status shouldBe OrderStatus.FAILED
    }

    @Test
    fun `should update status in DB when event received from PaymentSucceed topic`() {
        orderRepository.save(order).block()
        val orderService = OrderService(orderRepository, kafkaTemplate, "OrderPlaced")
        val updateInDb = orderService.updateStatusInDB(OrderStatusChangeEvent("8d8b30e3-de52-4f1c-a71c-9905a8043dac"), "PaymentSucceed")

        StepVerifier.create(updateInDb)
                .expectSubscription()
                .consumeNextWith {
                    it shouldBe true
                }
                .verifyComplete()
        val updatedOrder = orderRepository.findById(order.orderId).block()!!
        updatedOrder.status shouldBe OrderStatus.IN_PROGRESS
    }

    @Test
    fun `should update status in DB when event received from PaymentFailed topic`() {
        orderRepository.save(order).block()
        val orderService = OrderService(orderRepository, kafkaTemplate, "OrderPlaced")
        val updateInDb = orderService.updateStatusInDB(OrderStatusChangeEvent("8d8b30e3-de52-4f1c-a71c-9905a8043dac"), "PaymentFailed")

        StepVerifier.create(updateInDb)
                .expectSubscription()
                .consumeNextWith {
                    it shouldBe true
                }
                .verifyComplete()
        val updatedOrder = orderRepository.findById(order.orderId).block()!!
        updatedOrder.status shouldBe OrderStatus.FAILED
    }

    @Test
    fun `should NOT update status in DB when event received from unknown topic`() {
        orderRepository.save(order).block()
        val orderService = OrderService(orderRepository, kafkaTemplate, "Non-Existent-Topic")
        val updateInDb = orderService.updateStatusInDB(OrderStatusChangeEvent("8d8b30e3-de52-4f1c-a71c-9905a8043dac"), "Non-Existent-Topic")

        StepVerifier.create(updateInDb)
                .expectSubscription()
                .consumeNextWith {
                    it shouldBe false
                }
                .verifyComplete()
        val updatedOrder = orderRepository.findById(order.orderId).block()!!
        updatedOrder.status shouldBe OrderStatus.RECEIVED
    }

    @Test
    fun `should NOT update status in DB when record dosent exist in DB`() {
        val orderService = OrderService(orderRepository, kafkaTemplate, "OrderPlaced")
        val updateInDb = orderService.updateStatusInDB(OrderStatusChangeEvent("8d8b30e3-de52-4f1c-a71c-9905a8043dac"), "OrderPlaced")

        StepVerifier.create(updateInDb)
                .expectSubscription()
                .consumeNextWith {
                    it shouldBe false
                }
                .verifyComplete()
    }
}