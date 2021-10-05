package com.orderservice.kafka

import com.orderservice.models.Order
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaProducerConfig(
        @Value("\${spring.kafka.bootstrap-servers}")
        val bootstrapServers: String
) {
    fun producerFactory(): ProducerFactory<String, Order> {
        val properties = hashMapOf<String, Any>()
        properties[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        properties[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        properties[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return DefaultKafkaProducerFactory(properties)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Order> {
        return KafkaTemplate(producerFactory())
    }
}