# SSE + DLQ Implementation — 2026-03-07

## SSE (Server-Sent Events)
- Registry: `infrastructure/sse/SseEmitterRegistry.java` — ConcurrentHashMap<UUID, SseEmitter>, timeout 5min
- Controller: `presentation/controller/PedidoSseController.java` — GET /pedidos/stream (authenticated)
  - Looks up real UUID via UsuarioRepository.findByEmail(userDetails.getUsername())
- Consumer: `PedidoRabbitMQConsumer` — calls sseRegistry.sendEvent(usuarioId, eventName, message) after each event

## DLQ Requeue
- Service: `infrastructure/messaging/DlqRequeueService.java`
  - requeue(dlqName) — reads via rabbitTemplate.receive(dlq, 500ms timeout) and republishes to pedidos.exchange
  - requeueAll() — loops all 4 DLQs
- Controller: `presentation/controller/DlqAdminController.java` (ADMIN only)
  - GET /admin/dlq/queues
  - POST /admin/dlq/requeue/{queue}
  - POST /admin/dlq/requeue-all

## Rate Limiting
- Filter: `infrastructure/security/RateLimitingFilter.java`
  - Bucket4j in-memory, 10 req/min per IP
  - Applied to /auth/login and /auth/register only
  - Registered in SecurityConfig before JwtAuthenticationFilter
  - Dependency: com.bucket4j:bucket4j-core:8.10.1 in pom.xml

## Actuator / Prometheus
- application.properties: management.endpoints.web.exposure.include includes prometheus
- management.health.rabbit.enabled=true
- application-dev.properties: same, plus H2 console enabled, Flyway disabled
