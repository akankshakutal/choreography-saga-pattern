package com.orderservice.kafka

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

//@Configuration
class KafkaAdminConfig(
        @Value("\${kafka.producer.topic.name}")
        val topicName: String
) {
    @Bean
    fun newTopic(): NewTopic {
        return TopicBuilder.name(topicName).partitions(1).replicas(1).build()
    }
}