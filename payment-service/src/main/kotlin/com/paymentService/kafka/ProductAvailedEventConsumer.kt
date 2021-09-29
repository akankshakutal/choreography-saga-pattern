package com.paymentService.kafka

import com.paymentService.models.ProductAvailed
import com.paymentService.repository.Transaction
import com.paymentService.repository.TransactionStatus
import com.paymentService.repository.TransactionsRepository
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class ProductAvailedEventConsumer(val transactionRepository: TransactionsRepository) {

    @Value("orderService.url")
    lateinit var uri: String

    @Bean
    fun topic() = NewTopic(KafkaConfig.productServiceTopicName, 5, 1)

    @KafkaListener(topics = [KafkaConfig.productServiceTopicName])
    fun listen(productAvailedEvent: ProductAvailed) {
        val amount = RestTemplate().getForEntity("$uri${productAvailedEvent.orderId}", Amount::class.java).body!!
        val transaction = Transaction(productAvailedEvent.orderId, amount.value, TransactionStatus.PENDING)
        transactionRepository.save(transaction)
    }
}

data class Amount(val value: Double)