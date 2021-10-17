package com.orderservice.service

import com.orderservice.models.AddressType
import com.orderservice.models.Item
import com.orderservice.models.Order
import com.orderservice.models.OrderRequest
import com.orderservice.models.OrderStatus
import com.orderservice.models.ShippingAddress
import com.orderservice.repository.OrderRepository
import io.kotlintest.shouldBe
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient

@EmbeddedKafka(topics = ["OrderPlaced"], partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:9092", "port=9092"])
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class OrderServiceIntegrationTest {
    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    var kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry? = null

    @Value("\${kafka.producer.topic.name}")
    lateinit var producerTopic: String

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

    lateinit var consumer: Consumer<String, Order>

    @BeforeEach
    private fun setUp() {
        val props = KafkaTestUtils.consumerProps("group1", true.toString(), embeddedKafkaBroker)
        consumer = DefaultKafkaConsumerFactory(props, StringDeserializer(), JsonDeserializer(Order::class.java)).createConsumer()
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer)
    }

    @AfterEach
    private fun tearDown() {
        consumer.close()
    }

    @Test
    @Timeout(5)
    fun `should submit the order successfully`() {
        webTestClient.post()
                .uri("http://localhost:8080/order/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus()
                .isAccepted
                .expectBody()
                .jsonPath("orderId").isNotEmpty

        val publishedOrder = consumeFromTopic()
        assertOrder(publishedOrder, order)

        val orderSavedInDb = orderRepository.findAll().next().block()!!
        assertOrder(orderSavedInDb, order)
    }

    private fun consumeFromTopic(): Order {
        val result = KafkaTestUtils.getSingleRecord(consumer, producerTopic).value()
        return result
    }
}

private fun assertOrder(actual: Order, expected: Order) {
    actual.customerId shouldBe expected.customerId
    actual.items shouldBe expected.items
    actual.totalAmount shouldBe expected.totalAmount
    actual.shippingAddress shouldBe expected.shippingAddress
    actual.status shouldBe OrderStatus.RECEIVED
}