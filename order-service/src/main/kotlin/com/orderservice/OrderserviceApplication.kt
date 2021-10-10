package com.orderservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka

@SpringBootApplication
@EnableKafka
class OrderserviceApplication

fun main(args: Array<String>) {
	runApplication<OrderserviceApplication>(*args)
}
