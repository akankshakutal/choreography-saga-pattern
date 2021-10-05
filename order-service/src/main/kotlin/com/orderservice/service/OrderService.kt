package com.orderservice.service

import com.orderservice.models.Order
import com.orderservice.models.OrderStatus
import com.orderservice.models.OrderStatusChangeEvent
import com.orderservice.models.OrderTotalAmountResponse
import com.orderservice.models.UnknownEventException
import com.orderservice.repository.OrderRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class OrderService(
        val orderRepository: OrderRepository,
        val kafkaTemplate: KafkaTemplate<String, Order>,
        @Value("\${kafka.producer.topic.name}")
        val producerTopicName: String
) {
    fun createOrder(order: Order): Mono<Order> {
        return publishOnKafka(order)
                .flatMap {
                    saveInDB(order)
                }
    }

    fun getOrderTotalAmount(orderId: String): Mono<OrderTotalAmountResponse> {
        return orderRepository.findById(orderId).map { it.totalAmount }.map { OrderTotalAmountResponse(it) }
    }

    private fun publishOnKafka(order: Order): Mono<SendResult<String, Order>> {
        return Mono.fromCallable {
            kafkaTemplate.send(producerTopicName, order.orderId, order).get()
        }
                .doOnSuccess {
                    println("Message published on Kafka")
                }
                .doOnError {
                    println("Error publishing to kafka.Error - ${it.message}")
                }
    }

    private fun saveInDB(order: Order): Mono<Order> {
        return orderRepository.save(order)
                .doOnError {
                    println("Error saving in DB.Error - ${it.message}")
                }
                .doOnSuccess {
                    println("Message published on Kafka")
                }
    }

    fun updateStatusInDB(orderStatusChangeEvent: OrderStatusChangeEvent, topic: String): Mono<Boolean> {
        val statusMono = Mono.fromCallable {
            when (topic) {
                "ProductAvailed" -> OrderStatus.IN_PROGRESS
                "ProductAvailFailed" -> OrderStatus.FAILED
                "PaymentSucceed" -> OrderStatus.IN_PROGRESS
                "PaymentFailed" -> OrderStatus.FAILED
                else -> throw UnknownEventException("Cannot process event from topic $topic")
            }
        }
        val orderMono = orderRepository.findById(orderStatusChangeEvent.orderId)
        return statusMono.zipWith(orderMono)
                .map {
                    it.t2.copy(status = it.t1)
                }
                .flatMap {
                    orderRepository.save(it)
                }
                .map {
                    true
                }
                .switchIfEmpty(Mono.just(false))
                .onErrorReturn(false)
    }
}