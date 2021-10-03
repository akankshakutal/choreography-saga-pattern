package com.paymentService.service

import com.paymentService.kafka.KafkaConfig
import com.paymentService.kafka.PaymentServiceEventProducer
import com.paymentService.models.PaymentDetails
import com.paymentService.models.PaymentFailedEvent
import com.paymentService.models.PaymentSucceedEvent
import com.paymentService.repository.CustomerBankAccount
import com.paymentService.repository.CustomerBankAccountRepository
import com.paymentService.repository.Transaction
import com.paymentService.repository.TransactionsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PaymentService(
    @Autowired private val producer: PaymentServiceEventProducer,
    @Autowired private val customerBankAccountRepository: CustomerBankAccountRepository,
    @Autowired private val transactionsRepository: TransactionsRepository
) {

    fun pay(paymentDetails: PaymentDetails): PaymentResponse {
        val customerBankAccount =
            customerBankAccountRepository.findByAccountNumberAndCvv(paymentDetails.accountNumber, paymentDetails.cvv)
        val transaction = transactionsRepository.findByOrderId(paymentDetails.orderId)
        return if (customerBankAccount.balance >= transaction.amount)
            handleSuccessCase(customerBankAccount, transaction)
        else handleFailureCase(customerBankAccount, transaction)
    }

    private fun handleFailureCase(customerBankAccount: CustomerBankAccount, transaction: Transaction): PaymentResponse {
        producer.produce(
            KafkaConfig.paymentFailedTopicName,
            PaymentFailedEvent(customerBankAccount.id, transaction.orderId)
        )
        return PaymentResponse(PaymentStatus.FAILED, transaction.amount)
    }

    private fun handleSuccessCase(customerBankAccount: CustomerBankAccount, transaction: Transaction): PaymentResponse {
        customerBankAccount.balance -= transaction.amount
        customerBankAccountRepository.save(customerBankAccount)
        producer.produce(
            KafkaConfig.paymentSucceedTopicName,
            PaymentSucceedEvent(customerBankAccount.id, transaction.orderId)
        )
        return PaymentResponse(PaymentStatus.SUCCESS, transaction.amount)
    }
}

data class PaymentResponse(val status: PaymentStatus, val amount: Double)

enum class PaymentStatus { SUCCESS, FAILED }