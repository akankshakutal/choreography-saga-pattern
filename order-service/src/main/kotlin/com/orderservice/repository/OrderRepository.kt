package com.orderservice.repository

import com.orderservice.models.Order
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface OrderRepository  : ReactiveMongoRepository<Order,String>