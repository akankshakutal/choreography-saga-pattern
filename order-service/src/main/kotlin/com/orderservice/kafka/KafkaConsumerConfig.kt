package com.orderservice.kafka

import com.orderservice.models.OrderStatusChangeEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
@EnableKafka
class KafkaConsumerConfig(
        @Value("\${spring.kafka.bootstrap-servers}")
        val bootstrapServers: String
) {
    @Bean
    fun consumerFactory(): ConsumerFactory<String, OrderStatusChangeEvent> {
        val properties = hashMapOf<String, Any>()
        properties[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        properties[ConsumerConfig.GROUP_ID_CONFIG] = "order-status-change-event-group"
        return DefaultKafkaConsumerFactory(properties, StringDeserializer(), JsonDeserializer(OrderStatusChangeEvent::class.java))
    }

    @Bean
    fun kafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, OrderStatusChangeEvent>): ConcurrentKafkaListenerContainerFactory<String, OrderStatusChangeEvent> {
        val concurrentKafkaListenerContainerFactory = ConcurrentKafkaListenerContainerFactory<String, OrderStatusChangeEvent>()
        concurrentKafkaListenerContainerFactory.consumerFactory = consumerFactory
        return concurrentKafkaListenerContainerFactory
    }
}