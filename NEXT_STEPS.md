# Next Steps & Pending Work

## High Priority

*(All high-priority items completed — see Completed section below)*

## Medium Priority

- [ ] Write JUnit tests for entity business methods — `Pedido.pagar()`, `Pedido.enviar()`,
  `Pedido.cancelar()` state machine transitions should be unit tested.
- [ ] Write integration test for `PedidoRabbitMQProducer` — verify message is published
  to correct exchange with correct routing key (use `@SpringBootTest` + embedded RabbitMQ
  or Testcontainers).
- [ ] Add `CancelOrderUseCase` as a proper use case — currently cancellation in
  `AdminPedidoController` calls repository + `pedido.cancelar()` inline without publishing
  domain events. Move to `CancelOrderUseCase` following the same pattern as
  `PagarPedidoUseCase` and `EnviarPedidoUseCase`.
- [ ] HTML email templates with Thymeleaf — replace plain-text `SimpleMailMessage`.
- [ ] Add Flyway migration for any schema changes needed by the SSE/WebSocket feature.
- [ ] Add pagination to `GET /admin/pedidos`.

## Low Priority / Nice to Have

- [ ] Implement refresh token endpoint (`POST /auth/refresh`).
- [ ] Add product image upload (S3 or equivalent) instead of hardcoded image URLs.

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
- [x] Verify docker-compose.yml is tracked in git (manual: `git add docker-compose.yml`) — 2026-03-07
- [x] Fix `application-dev.properties` — now uses H2 in-memory DB + safe fallback defaults
  for all env vars (RABBITMQ, JWT, CORS, Mail). Devs can run with zero `.env` setup — 2026-03-07
- [x] DLQ retry logic — `DlqRequeueService` + `DlqAdminController` with
  `POST /admin/dlq/requeue/{queue}` and `POST /admin/dlq/requeue-all` — 2026-03-07
- [x] SSE real-time order status stream — `SseEmitterRegistry` + `PedidoSseController`
  (`GET /pedidos/stream`). Consumer pushes events on pedido.criado/pago/enviado/cancelado — 2026-03-07
- [x] Rate limiting on `/auth/login` and `/auth/register` — Bucket4j in-memory,
  10 req/min per IP, returns 429 + Retry-After header — 2026-03-07
- [x] Actuator `/actuator/prometheus` exposed — micrometer-registry-prometheus was already
  in pom.xml, just needed property config — 2026-03-07
- [x] RabbitMQ health check in `/actuator/health` — `management.health.rabbit.enabled=true` — 2026-03-07
