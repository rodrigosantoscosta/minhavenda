# minhavenda (E:\code\code2\minhavenda) — Project Overview

## Purpose
Spring Boot 3.2 REST API for MinhaVenda e-commerce platform. Clean Architecture + DDD.
This is the more advanced version — RabbitMQ is fully implemented.

## Tech Stack
- Java 17 + Spring Boot 3.2.1
- Spring Security 6 + JWT
- Spring Data JPA + PostgreSQL (prod) / H2 (dev)
- Flyway migrations V1–V10
- Spring AMQP + RabbitMQ (fully implemented: producer, consumer, DLQ)
- JavaMailSender + Mailhog (dev email)
- SpringDoc OpenAPI (Swagger)
- Lombok + MapStruct
- Maven (mvnw wrapper)
- Docker + docker-compose (PostgreSQL + RabbitMQ + Mailhog)
- Deployment: Railway

## Base package
br.com.minhavenda.minhavenda

## Key files
- src/main/resources/application.properties — base config (uses env vars)
- src/main/resources/db/migration/ — Flyway migrations V1–V10
- AGENTS.md — coding rules including Rule 11 (LAST_CHANGES + NEXT_STEPS)
- NEXT_STEPS.md — pending work
- LAST_CHANGES.md — session changelog

## RabbitMQ
- Exchange: pedidos.exchange (topic)
- Queues: pedidos.criado, pedidos.pago, pedidos.enviado, pedidos.cancelado
- DLX: pedidos.dlx with *.dlq queues
- Config: infrastructure/config/RabbitMQConfig.java
- Producer: infrastructure/messaging/producer/PedidoRabbitMQProducer.java
- Consumer: infrastructure/messaging/consumer/PedidoRabbitMQConsumer.java
- Message DTOs: infrastructure/messaging/dto/

## Server
- Port: 8080, Context path: /api
- RabbitMQ Management: http://localhost:15672
- Mailhog: http://localhost:8025
