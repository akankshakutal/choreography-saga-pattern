package com.orderservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka

@SpringBootApplication
@EnableKafka
class Orderservice2Application

fun main(args: Array<String>) {
	runApplication<Orderservice2Application>(*args)
}
