package com.paymentService.repository

import org.springframework.data.mongodb.repository.MongoRepository

interface TransactionsRepository : MongoRepository<Transaction, String>