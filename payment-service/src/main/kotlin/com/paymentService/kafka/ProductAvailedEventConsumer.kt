package com.paymentService.kafka

import com.paymentService.models.ProductAvailed
import com.paymentService.models.RollbackPayment
import com.paymentService.repository.CustomerBankAccountRepository
import com.paymentService.repository.Transaction
import com.paymentService.repository.TransactionStatus
import com.paymentService.repository.TransactionsRepository
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class ProductAvailedEventConsumer(
    val transactionRepository: TransactionsRepository,
    @Autowired private val customerBankAccountRepository: CustomerBankAccountRepository,
) {

    @Value("orderService.url")
    lateinit var uri: String

    @Bean
    fun topic() = NewTopic(KafkaConfig.productServiceTopicName, 5, 1)

    @KafkaListener(topics = [KafkaConfig.productServiceTopicName])
    fun listenProductServiceEvent(productAvailedEvent: ProductAvailed) {
        val amount = RestTemplate().getForEntity("$uri${productAvailedEvent.orderId}", Amount::class.java).body!!
        val transaction = Transaction(productAvailedEvent.orderId, amount.value, TransactionStatus.PENDING)
        transactionRepository.save(transaction)
    }

    @KafkaListener(topics = [KafkaConfig.shipmentServiceTopicName])
    fun listenShipmentServiceEvent(rollbackPayment: RollbackPayment) {
        val transaction = transactionRepository.findByOrderId(rollbackPayment.orderId)
        val customerBankAccount = customerBankAccountRepository.findByAccountNumber(transaction.accountNumber!!)
        customerBankAccount.balance += transaction.amount
        transaction.status = TransactionStatus.ROLLBACK
        customerBankAccountRepository.save(customerBankAccount)
        transactionRepository.save(transaction)
    }
}

data class Amount(val value: Double)