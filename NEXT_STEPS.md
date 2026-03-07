# Next Steps & Pending Work

## High Priority

- [ ] Verify `docker-compose.yml` is now tracked in git after the `.gitignore` fix —
  it was previously being ignored, which means PostgreSQL + RabbitMQ config may never
  have been committed. Run `git status` and commit if untracked.
- [ ] Add Dead Letter Queue retry logic in `PedidoRabbitMQConsumer` — currently failed
  messages land in `*.dlq` queues with no automated retry or alerting. Implement a
  scheduled job or manual requeue endpoint.
- [ ] Expose SSE or WebSocket endpoint fed by the RabbitMQ consumer so the frontend
  can receive real-time order status updates instead of polling `GET /meus-pedidos`
  every 30 seconds.
- [ ] Add `application-dev.properties` (without secrets) committed to the repo so new
  developers can run immediately with `mvn spring-boot:run` and get H2 + Mailhog + Swagger.

## Medium Priority

- [ ] Write JUnit tests for entity business methods — `Pedido.pagar()`, `Pedido.enviar()`,
  `Pedido.cancelar()` state machine transitions should be unit tested.
- [ ] Write integration test for `PedidoRabbitMQProducer` — verify message is published
  to correct exchange with correct routing key (use `@SpringBootTest` + embedded RabbitMQ
  or Testcontainers).
- [ ] Add `CancelOrderUseCase` as a proper use case — currently cancellation in
  `AdminPedidoController` may be calling repository + eventPublisher inline.
- [ ] HTML email templates with Thymeleaf — replace plain-text `SimpleMailMessage`.
- [ ] Add Flyway migration for any schema changes needed by the SSE/WebSocket feature.
- [ ] Add pagination to `GET /admin/pedidos`.

## Low Priority / Nice to Have

- [ ] Rate limiting on auth endpoints (`/auth/login`, `/auth/register`) to prevent
  brute-force attacks.
- [ ] Implement refresh token endpoint (`POST /auth/refresh`).
- [ ] Add product image upload (S3 or equivalent) instead of hardcoded image URLs.
- [ ] Actuator metrics exposure (`/actuator/prometheus`) for Prometheus scraping in prod.
- [ ] Add RabbitMQ connection health check to `/actuator/health`.

## Completed

- [x] RabbitMQ producer + consumer + message DTOs + DLQ config — `infrastructure/messaging/`
- [x] Domain events system (PedidoCriadoEvent, PedidoPagoEvent, PedidoEnviadoEvent,
  PedidoCanceladoEvent) with async Spring listeners
- [x] Email notifications via JavaMailSender + Mailhog dev setup
- [x] JWT authentication with Spring Security 6
- [x] Flyway migrations V1–V10
- [x] Clean Architecture layer separation
- [x] Swagger/OpenAPI 3 documentation
- [x] Docker Compose with PostgreSQL + RabbitMQ + Mailhog
- [x] README.md full rewrite — done 2026-03-07
- [x] .gitignore reorganised + docker-compose.yml unignored — done 2026-03-07
- [x] LAST_CHANGES.md and NEXT_STEPS.md created — done 2026-03-07
- [x] AGENTS.md Rule 11 added — done 2026-03-07
