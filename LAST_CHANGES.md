## 2026-03-07 — Code review fixes: docker-compose, tests

### Files modified
- `docker-compose.yml` — added Mailhog service (SMTP 1025, Web UI 8025); added all missing app env vars (JWT_SECRET, JWT_EXPIRATION, MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD, CORS_ALLOWED_ORIGINS, RABBITMQ_HOST/PORT); added app healthcheck via curl + actuator/health; changed default SPRING_PROFILES_ACTIVE to prod for compose context
- `src/test/.../consumer/PedidoRabbitMQConsumerTest.java` — added `@Mock SseEmitterRegistry sseRegistry` (was missing, caused NPE on every test); added SSE delegation assertions (`verify(sseRegistry).sendEvent(...)`) for all four consumers; added DLQ rethrow test
- `src/test/.../usecase/pedido/PagarPedidoUseCaseTest.java` — replaced redundant `devePublicarEventos` with `devePublicarPedidoPagoEventComMetodoCorreto` using `ArgumentCaptor` to assert event type and `metodoPagamento` field
- `src/test/.../producer/PedidoRabbitMQProducerTest.java` — all four producer tests now assert both exchange name (`pedidos.exchange`) and routing key; extracted `EXPECTED_EXCHANGE` constant; renamed test methods for clarity

---

$1 & Low priority NEXT_STEPS implemented

### Files created
- `src/main/resources/application-dev.properties` — full rewrite: H2 in-memory DB, all env vars have safe fallback defaults (RABBITMQ guest, JWT dev key, Mailhog port 1025, CORS localhost). Devs can now run `mvn spring-boot:run` with zero `.env` setup
- `src/main/java/.../infrastructure/messaging/DlqRequeueService.java` — reprocessa mensagens das DLQs via `RabbitTemplate.receive()` + republish no exchange principal. Metodos: `requeue(dlqName)`, `requeueAll()`, `stats()`
- `src/main/java/.../presentation/controller/DlqAdminController.java` — endpoints `GET /admin/dlq/queues`, `POST /admin/dlq/requeue/{queue}`, `POST /admin/dlq/requeue-all` (ADMIN only)
- `src/main/java/.../infrastructure/sse/SseEmitterRegistry.java` — registro thread-safe de SseEmitters por userId (ConcurrentHashMap). Timeout 5min. Metodos: `register()`, `sendEvent()`, `remove()`
- `src/main/java/.../presentation/controller/PedidoSseController.java` — `GET /pedidos/stream` (text/event-stream). Registra emitter do usuario autenticado. Lookup real UUID via UsuarioRepository
- `src/main/java/.../infrastructure/security/RateLimitingFilter.java` — Bucket4j in-memory rate limiting em `/auth/login` e `/auth/register`: 10 req/min por IP. Retorna 429 + header Retry-After

### Files modified
- `src/main/java/.../infrastructure/messaging/consumer/PedidoRabbitMQConsumer.java` — injetado `SseEmitterRegistry`; apos processar cada evento RabbitMQ, chama `sseRegistry.sendEvent(usuarioId, eventName, message)`
- `src/main/java/.../infrastructure/config/SecurityConfig.java` — injetado `RateLimitingFilter`; registrado com `.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)`
- `src/main/resources/application.properties` — adicionado `prometheus` ao exposure list, `management.endpoint.prometheus.enabled=true`, `management.health.rabbit.enabled=true`
- `pom.xml` — adicionado `bucket4j-core:8.10.1`
- `NEXT_STEPS.md` — itens concluidos movidos para secao Completed; Medium priority mantido intacto

### Manual action required
- Run `git add docker-compose.yml && git commit -m "chore: track docker-compose.yml"` if not yet committed

---

## 2026-03-07 — Project housekeeping: .gitignore, README.md, and Serena onboarding

### Files changed
- `.gitignore` — full rewrite with organised sections; key additions: `.serena/cache/` and `.serena/project.local.yml` (machine-specific Serena files, must not be committed); fixed critical bug where `docker-compose.yml` was being ignored (it is a source file and must be tracked); removed `compose.yaml` blanket ignore; kept `.serena/memories/` committable for shared agent context; added `production-opts.txt`
- `README.md` — full rewrite replacing boilerplate with complete project documentation: tech stack table, setup instructions, environment variables table (including all RabbitMQ vars), project structure tree with all messaging packages, architecture diagram, RabbitMQ bridge architecture section with queue/DLQ table, domain events table linking events to queues, full API reference, database schema table, available scripts, Mailhog + RabbitMQ Management UI guides, deployment info, links to AGENTS.md / NEXT_STEPS.md / LAST_CHANGES.md
- `NEXT_STEPS.md` — created: living roadmap with High / Medium / Low priorities
- `LAST_CHANGES.md` — created: this file; session changelog going forward
- `AGENTS.md` — added Rule 11: after every agent session with code changes, agent must update LAST_CHANGES.md and NEXT_STEPS.md

### Notes
- The critical `.gitignore` bug (docker-compose.yml ignored) means the compose file may never have been tracked in git. Worth running `git status` to confirm and committing it if needed.
- This project is more advanced than `E:\code\minhavenda` — RabbitMQ producer, consumer, message DTOs, DLQ config, and V10 migration are all present here.

---
