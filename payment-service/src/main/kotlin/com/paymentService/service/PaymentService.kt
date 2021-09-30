package com.paymentService.service

import com.paymentService.kafka.PaymentServiceEventProducer
import com.paymentService.models.PaymentDetails
import com.paymentService.models.PaymentFailedEvent
import com.paymentService.models.PaymentSucceedEvent
import com.paymentService.repository.CustomerBankAccountRepository
import com.paymentService.repository.TransactionsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PaymentService(
    @Autowired private val paymentServiceEventProducer: PaymentServiceEventProducer,
    @Autowired private val customerBankAccountRepository: CustomerBankAccountRepository,
    @Autowired private val transactionsRepository: TransactionsRepository
) {

    @Value("spring.kafka.producer.paymentSucceed.topic")
    var paymentSucceedTopicName: String = ""

    @Value("spring.kafka.producer.paymentFailed.topic")
    var paymentFailedTopicName: String = ""

    fun pay(paymentDetails: PaymentDetails): PaymentResponse {
        val customerBankAccount =
            customerBankAccountRepository.findByAccountNumberAndCvv(paymentDetails.accountNumber, paymentDetails.cvv)
        val transaction = transactionsRepository.findByOrderId(paymentDetails.orderId)
        if (customerBankAccount.balance >= transaction.amount) {
            customerBankAccount.balance -= transaction.amount
            customerBankAccountRepository.save(customerBankAccount)
            paymentServiceEventProducer.produce(
                paymentSucceedTopicName,
                PaymentSucceedEvent(customerBankAccount.id, transaction.orderId)
            )
        } else {
            val paymentSucceedEvent = PaymentFailedEvent("paymentId", "orderId")
            paymentServiceEventProducer.produce(paymentFailedTopicName, paymentSucceedEvent)
        }
        return PaymentResponse("SUCCESS", transaction.amount)
    }
}

data class PaymentResponse(val status: String, val amount: Double)