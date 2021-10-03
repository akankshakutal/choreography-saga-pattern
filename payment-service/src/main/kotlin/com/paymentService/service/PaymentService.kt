package com.paymentService.service

import com.paymentService.kafka.KafkaConfig
import com.paymentService.kafka.PaymentServiceEventProducer
import com.paymentService.models.*
import com.paymentService.repository.*
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

    fun addAccount(accountDetails: AccountDetails): AccountCreationResponse {
        val customerBankAccount = CustomerBankAccount(
            name = accountDetails.name,
            accountNumber = accountDetails.accountNumber,
            cvv = accountDetails.cvv,
            balance = accountDetails.balance
        )
        customerBankAccountRepository.save(customerBankAccount)
        return AccountCreationResponse(AccountStatus.CREATED)
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
        transaction.status = TransactionStatus.SUCCESS
        customerBankAccountRepository.save(customerBankAccount)
        transactionsRepository.save(transaction)
        producer.produce(
            KafkaConfig.paymentSucceedTopicName,
            PaymentSucceedEvent(customerBankAccount.id, transaction.orderId)
        )
        return PaymentResponse(PaymentStatus.SUCCESS, transaction.amount)
    }
}