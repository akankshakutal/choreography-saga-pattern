package com.paymentService.service

import com.paymentService.kafka.KafkaConfig
import com.paymentService.kafka.KafkaConfig.paymentSucceedTopicName
import com.paymentService.kafka.PaymentServiceEventProducer
import com.paymentService.models.*
import com.paymentService.repository.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Component

@Component
class PaymentService(
    @Autowired private val producer: PaymentServiceEventProducer,
    @Autowired private val customerBankAccountRepository: CustomerBankAccountRepository,
    @Autowired private val transactionsRepository: TransactionsRepository
) {

    fun pay(paymentDetails: PaymentDetails): PaymentResponse {
        val customerBankAccount = customerBankAccount(paymentDetails.accountNumber, paymentDetails.cvv)
        val transaction = transaction(paymentDetails)
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

    private fun transaction(paymentDetails: PaymentDetails): Transaction {
        return try {
            transactionsRepository.findByOrderId(paymentDetails.orderId)
        } catch (exception: EmptyResultDataAccessException) {
            throw OrderNotFoundException()
        }
    }

    private fun handleFailureCase(customerBankAccount: CustomerBankAccount, transaction: Transaction): PaymentResponse {
        producer.produce(
            KafkaConfig.paymentFailedTopicName,
            PaymentFailedEvent(customerBankAccount.id, transaction.orderId)
        )
        return PaymentResponse(PaymentStatus.FAILED, transaction.amount)
    }

    private fun customerBankAccount(accountNumber: Int, cvv: Int): CustomerBankAccount {
        return try {
            customerBankAccountRepository.findByAccountNumberAndCvv(accountNumber, cvv)
        } catch (exception: EmptyResultDataAccessException) {
            throw BankAccountNotFoundException()
        }
    }

    private fun handleSuccessCase(customerBankAccount: CustomerBankAccount, transaction: Transaction): PaymentResponse {
        customerBankAccount.balance -= transaction.amount
        transaction.status = TransactionStatus.SUCCESS
        customerBankAccountRepository.save(customerBankAccount)
        transactionsRepository.save(transaction)
        val paymentSucceedEvent = PaymentSucceedEvent(customerBankAccount.id, transaction.orderId)
        producer.produce(paymentSucceedTopicName, paymentSucceedEvent)
        return PaymentResponse(PaymentStatus.SUCCESS, transaction.amount)
    }
}

class BankAccountNotFoundException : Throwable()
class OrderNotFoundException : Throwable()