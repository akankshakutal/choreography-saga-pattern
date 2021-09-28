# Microservice: choreography saga pattern

This repo contains a docker-compose setup for 

## Services
- order-service
- product-service
- payment-service
- shipment-service

## Tools

### mongodb
accessible from services at `mongodb://root:example@mongo:27017`

### mongo-express
accessible from browser at http://localhost:8081/ 
![mongo](imgs/img2.png)

### kafka
accessible from services at `kafka:9092`

### zookeeper
accessible from kafka at `zookeeper:2181`

### kowl
accessible from browser at http://localhost:8080/
![kowl](imgs/img1.png)
