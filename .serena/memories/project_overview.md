# MinhaVenda - Visão Geral

## Propósito
E-commerce full-stack com Spring Boot (Clean Architecture/DDD) + React (Vite).

## Tech Stack
- **Backend:** Java 17, Spring Boot 3.2.1, Spring Data JPA, Spring Security, Flyway, Lombok
- **Messaging:** Spring AMQP / RabbitMQ (spring-boot-starter-amqp já no pom.xml)
- **Database:** PostgreSQL (prod) / H2 (dev/test)
- **Frontend:** React 19, Vite 5, Tailwind CSS, Axios, React Router v7
- **Docs:** SpringDoc OpenAPI (Swagger)

## Package Base
`br.com.minhavenda.minhavenda`

## Arquitetura (Clean Architecture / DDD)
```
presentation/   → REST Controllers
application/    → Use Cases, DTOs, Mappers, Events
domain/         → Entities, Value Objects, Domain Events, Business Rules
infrastructure/ → JPA Repositories, Email, Notification, Security
config/         → SecurityConfig, GlobalExceptionHandler
```

## Domain Events já implementados (Spring ApplicationEventPublisher)
- `PedidoCriadoEvent` → disparado em `FinalizarCheckoutUseCase`
- `PedidoPagoEvent` → disparado em `PagarPedidoUseCase`
- `PedidoEnviadoEvent` → disparado em `EnviarPedidoUseCase`
- `PedidoCanceladoEvent` → disparado na entidade `Pedido`

## Event Listener atual
`infrastructure/event/listener/PedidoEventListener.java`
- Usa `@EventListener` + `@Async` do Spring
- Chama `EmailService` e `NotificationService`
- TODO: Dead Letter Queue já anotado nos catch blocks
