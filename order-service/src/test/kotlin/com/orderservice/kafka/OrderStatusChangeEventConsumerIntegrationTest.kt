package com.orderservice.kafka

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.orderservice.models.AddressType
import com.orderservice.models.Item
import com.orderservice.models.Order
import com.orderservice.models.OrderRequest
import com.orderservice.models.OrderStatus
import com.orderservice.models.OrderStatusChangeEvent
import com.orderservice.models.ShippingAddress
import com.orderservice.repository.OrderRepository
import io.kotlintest.shouldBe
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@EmbeddedKafka(topics = ["ProductAvailed"], partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:9092", "port=9092"])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@TestPropertySource(properties = [
    "kafka.consumer.topic.names=ProductAvailed"
]
)
@Import(TestKafkaProducerConfig::class)
internal class OrderStatusChangeEventConsumerIntegrationTest {
    @Autowired
    lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    lateinit var kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry

    @Autowired
    lateinit var testKafkaTemplate: KafkaTemplate<String, String>

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

    private val orderStatusChangeEvent = OrderStatusChangeEvent(orderId = "8d8b30e3-de52-4f1c-a71c-9905a8043dac")

    @BeforeEach
    private fun setUp() {
        orderRepository.deleteAll().block()
        kafkaListenerEndpointRegistry.listenerContainers.forEach {
            ContainerTestUtils.waitForAssignment(it, embeddedKafkaBroker.partitionsPerTopic)
        }
    }

    @AfterEach
    private fun tearDown(){
        kafkaListenerEndpointRegistry.listenerContainers.forEach {
            it.stop()
        }
    }

    @Test
    fun `should process the event successfully`() {
        val countDownLatch = CountDownLatch(1)
        val event = jacksonObjectMapper().writeValueAsString(orderStatusChangeEvent)
        orderRepository.save(order).block()
        testKafkaTemplate.send("ProductAvailed", order.orderId, event).get()
        countDownLatch.await(2, TimeUnit.SECONDS)
        val order = orderRepository.findById("8d8b30e3-de52-4f1c-a71c-9905a8043dac").block()!!
        order.status shouldBe OrderStatus.IN_PROGRESS
    }
}

@TestConfiguration
class TestKafkaProducerConfig(
        @Value("\${spring.kafka.bootstrap-servers}")
        val bootstrapServers: String
) {
    fun producerFactory(): ProducerFactory<String, String> {
        val properties = hashMapOf<String, Any>()
        properties[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        properties[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        properties[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        return DefaultKafkaProducerFactory(properties)
    }

    @Bean
    fun testKafkaTemplate(): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory())
    }
}