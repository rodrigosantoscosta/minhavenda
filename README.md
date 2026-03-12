# MinhaVenda — Backend

Spring Boot 3.2 REST API for the MinhaVenda e-commerce platform, built with Clean Architecture and Domain-Driven Design.

**Live URLs:**
- Frontend: https://minhavenda-frontend.vercel.app
- Frontend repo: https://github.com/rodrigosantoscosta/minhavenda-frontend
- Backend API: https://minhavenda-production.up.railway.app/api
- Swagger (local): http://localhost:8080/api/swagger-ui.html

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.1 |
| Security | Spring Security 6 + JWT |
| Persistence | Spring Data JPA + Hibernate |
| Database (prod) | PostgreSQL 15 |
| Database (dev) | H2 (in-memory) |
| Migrations | Flyway (V1–V10) |
| Messaging | RabbitMQ (Spring AMQP) — fully implemented |
| Email | JavaMailSender + Mailhog (dev) |
| API Docs | SpringDoc OpenAPI 3 (Swagger) |
| Monitoring | Spring Actuator + Micrometer Prometheus |
| Rate Limiting | Bucket4j (in-memory, per IP) |
| Boilerplate | Lombok + MapStruct |
| Build | Maven (mvnw wrapper included) |
| Containers | Docker + Docker Compose |
| Deployment | Railway |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+ (or use `./mvnw` wrapper — no installation needed)
- Docker (for PostgreSQL, RabbitMQ, and Mailhog)

### 1. Clone the repository

```bash
git clone https://github.com/rodrigosantoscosta/minhavenda.git
cd minhavenda
```

### 2. Set up environment variables

```bash
cp .env.example .env
# Edit .env with your local values
```

See [Environment Variables](#environment-variables) for the full list.

### 3. Start dev dependencies

```bash
# Starts PostgreSQL + RabbitMQ + Mailhog
docker compose up -d
```

### 4. Run the application

```bash
# Dev profile — H2 in-memory DB, zero .env required
./mvnw spring-boot:run

# With PostgreSQL + RabbitMQ from Docker
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

> **Zero-config dev:** `application-dev.properties` ships with safe fallback defaults for all env vars (H2, RabbitMQ guest, Mailhog on port 1025, JWT dev key). New developers can run the app immediately with no `.env` setup.

### 5. Access local services

| Service | URL |
|---|---|
| REST API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/api/swagger-ui.html |
| H2 Console (dev) | http://localhost:8080/api/h2-console |
| RabbitMQ Management | http://localhost:15672 (guest / guest) |
| Mailhog (email) | http://localhost:8025 |
| Actuator health | http://localhost:8080/api/actuator/health |
| Prometheus metrics | http://localhost:8080/api/actuator/prometheus |

---

## Environment Variables

Copy `.env.example` to `.env` and fill in your values. Never commit `.env`.

| Variable | Description | Default / Example |
|---|---|---|
| `SPRING_DATASOURCE_URL` | JDBC URL for PostgreSQL | `jdbc:postgresql://localhost:5432/minhavenda` |
| `SPRING_DATASOURCE_USERNAME` | DB username | `minhavenda` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | `secret` |
| `JWT_SECRET` | JWT signing secret (min 32 chars) | — |
| `JWT_EXPIRATION` | Token TTL in milliseconds | `86400000` (24h) |
| `RABBITMQ_HOST` | RabbitMQ hostname | `localhost` |
| `RABBITMQ_PORT` | RabbitMQ AMQP port | `5672` |
| `RABBITMQ_USER` | RabbitMQ username | `guest` |
| `RABBITMQ_PASS` | RabbitMQ password | `guest` |
| `MAIL_HOST` | SMTP host | `localhost` (dev) |
| `MAIL_PORT` | SMTP port | `1025` (Mailhog) |
| `MAIL_USERNAME` | SMTP username | — |
| `MAIL_PASSWORD` | SMTP password | — |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | `http://localhost:5173` |

---

## Project Structure

```
src/main/java/br/com/minhavenda/minhavenda/
├── presentation/                  # REST controllers (HTTP layer only)
│   └── controller/
│       ├── AuthenticationController.java
│       ├── ProdutoController.java
│       ├── PedidoController.java
│       ├── PedidoSseController.java    # GET /pedidos/stream (SSE real-time updates)
│       ├── CarrinhoController.java
│       ├── CategoriaController.java
│       ├── EstoqueController.java
│       ├── PerfilController.java
│       ├── AdminPedidoController.java
│       └── DlqAdminController.java     # POST /admin/dlq/requeue (DLQ management)
│
├── application/                   # Use Cases + DTOs + Mappers + Event publisher
│   ├── dto/
│   │   ├── auth/
│   │   ├── carrinho/
│   │   ├── pedido/
│   │   └── produto/
│   ├── event/
│   │   └── DomainEventPublisher.java   # Publishes to Spring AND RabbitMQ
│   ├── mapper/
│   └── usecase/
│       ├── auth/
│       ├── categoria/
│       ├── estoque/
│       ├── pedido/
│       │   ├── FinalizarCheckoutUseCase.java
│       │   ├── PagarPedidoUseCase.java
│       │   └── EnviarPedidoUseCase.java
│       ├── produto/
│       └── usuario/
│
├── domain/                        # Core business — zero framework deps
│   ├── entity/                    # Aggregate roots with rich behavior
│   │   ├── Pedido.java            # pagar(), enviar(), cancelar()
│   │   ├── Usuario.java
│   │   ├── Produto.java
│   │   ├── Carrinho.java
│   │   └── Estoque.java
│   ├── enums/
│   │   ├── StatusPedido.java      # CRIADO, PAGO, ENVIADO, ENTREGUE, CANCELADO
│   │   ├── TipoUsuario.java       # ADMIN, CLIENTE
│   │   └── StatusCarrinho.java
│   ├── event/
│   │   └── pedido/
│   │       ├── PedidoCriadoEvent.java
│   │       ├── PedidoPagoEvent.java
│   │       ├── PedidoEnviadoEvent.java
│   │       └── PedidoCanceladoEvent.java
│   ├── exception/
│   │   ├── BusinessException.java
│   │   ├── ResourceNotFoundException.java
│   │   └── EntityAlreadyExistsException.java
│   └── valueobject/
│       ├── Money.java
│       └── Email.java
│
├── infrastructure/                # External concerns (framework allowed)
│   ├── config/
│   │   ├── RabbitMQConfig.java    # Exchange, queues, DLX, DLQ configuration
│   │   └── SecurityConfig.java
│   ├── persistence/
│   │   └── repository/            # Spring Data JPA repositories
│   ├── security/
│   │   ├── JwtService.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── CustomUserDetailsService.java
│   │   └── RateLimitingFilter.java     # Bucket4j — 10 req/min/IP on /auth/**
│   ├── event/
│   │   └── listener/
│   │       └── PedidoEventListener.java  # Bridge: Spring events → email + RabbitMQ
│   ├── messaging/
│   │   ├── producer/
│   │   │   └── PedidoRabbitMQProducer.java
│   │   ├── consumer/
│   │   │   └── PedidoRabbitMQConsumer.java  # Pushes SSE events after each message
│   │   ├── dto/                   # RabbitMQ message payloads
│   │   │   ├── PedidoCriadoMessage.java
│   │   │   ├── PedidoPagoMessage.java
│   │   │   ├── PedidoEnviadoMessage.java
│   │   │   └── PedidoCanceladoMessage.java
│   │   └── DlqRequeueService.java  # Requeue messages from *.dlq back to main exchange
│   ├── sse/
│   │   └── SseEmitterRegistry.java # Thread-safe SSE emitter registry (userId → emitter)
│   └── notification/
│       ├── EmailService.java
│       ├── EmailServiceImpl.java
│       └── NotificationService.java
│
└── config/
    └── GlobalExceptionHandler.java

src/main/resources/
├── application.properties         # Base config (env vars only)
├── application-dev.properties     # H2 + Mailhog + Swagger enabled
└── db/migration/
    ├── V1__create_tables.sql
    ├── V2__create_indexes.sql
    ├── V3__insert_categorias.sql
    ├── V4__insert_usuarios.sql
    ├── V5__insert_produtos.sql
    ├── V6-V9__update_url_imagem.sql
    └── V10__add_column_pedido.sql
```

---

## Architecture

Clean Architecture with strict inward dependencies (domain has zero framework imports):

```
Presentation → Application → Domain ← Infrastructure
```

**Layer responsibilities:**

- **Presentation** — HTTP in/out only. Delegates to use cases.
- **Application** — Orchestrates use cases. Publishes domain events. No business rules.
- **Domain** — Entities with rich behavior. Value objects. Domain events. No framework.
- **Infrastructure** — JPA, security, email, RabbitMQ. Implements domain interfaces.

---

## Messaging Architecture (RabbitMQ)

This project uses a **Bridge Pattern**: the existing Spring `@EventListener` chain is enriched with RabbitMQ publishing. Domain events flow through both channels simultaneously.

```
Entity method
    └─ registrarEvento(PedidoXEvent)
         └─ Use Case: eventPublisher.publishAll()
              ├─ Spring ApplicationEventPublisher
              │    └─ PedidoEventListener (@Async)
              │         ├─ EmailService (send email)
              │         └─ PedidoRabbitMQProducer (publish to exchange)
              │              └─ pedidos.exchange (topic)
              │                   ├─ pedidos.criado
              │                   ├─ pedidos.pago
              │                   ├─ pedidos.enviado
              │                   └─ pedidos.cancelado
              └─ PedidoRabbitMQConsumer (listening on queues)
                   ├─ NotificationService (in-app notifications)
                   └─ SseEmitterRegistry.sendEvent() → frontend via SSE
```

### Queue Configuration

| Exchange | Type | Dead Letter Exchange |
|---|---|---|
| `pedidos.exchange` | topic | `pedidos.dlx` |

| Queue | Routing Key | DLQ |
|---|---|---|
| `pedidos.criado` | `pedido.criado` | `pedidos.criado.dlq` |
| `pedidos.pago` | `pedido.pago` | `pedidos.pago.dlq` |
| `pedidos.enviado` | `pedido.enviado` | `pedidos.enviado.dlq` |
| `pedidos.cancelado` | `pedido.cancelado` | `pedidos.cancelado.dlq` |

---

## Domain Events

| Event | Trigger | Email | RabbitMQ queue | SSE event |
|---|---|---|---|---|
| `PedidoCriadoEvent` | `FinalizarCheckoutUseCase` | Confirmation | `pedidos.criado` | `pedido.criado` |
| `PedidoPagoEvent` | `PagarPedidoUseCase` | Payment confirmed | `pedidos.pago` | `pedido.pago` |
| `PedidoEnviadoEvent` | `EnviarPedidoUseCase` | Shipping + tracking | `pedidos.enviado` | `pedido.enviado` |
| `PedidoCanceladoEvent` | `Pedido.cancelar()` | Cancellation notice | `pedidos.cancelado` | `pedido.cancelado` |

**Event lifecycle:**
1. Entity method changes state and calls `registrarEvento(new XEvent(...))`
2. Use case saves entity, calls `eventPublisher.publishAll(pedido.getDomainEvents())`
3. Use case calls `pedido.limparEventos()` to free memory
4. Spring dispatches async to `PedidoEventListener`
5. Listener calls `EmailService` and `PedidoRabbitMQProducer`
6. Producer publishes serialized message to RabbitMQ exchange
7. `PedidoRabbitMQConsumer` receives from queue and calls `NotificationService`

---

## API Reference

All endpoints prefixed with `/api`.

### Authentication

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/auth/register` | Public | Register new user |
| POST | `/auth/login` | Public | Login, returns JWT |

### Products

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/produtos` | Public | List products (paginated) |
| GET | `/produtos/{id}` | Public | Product detail |
| GET | `/produtos/buscar` | Public | Search with filters |
| POST | `/produtos` | Admin | Create product |

### Cart

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/carrinho` | User | Get current cart |
| POST | `/carrinho/items` | User | Add item |
| PUT | `/carrinho/items/{id}` | User | Update quantity |
| DELETE | `/carrinho/items/{id}` | User | Remove item |

### Orders

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/meus-pedidos` | User | List user's orders |
| GET | `/pedidos/{id}` | User | Order detail |
| POST | `/checkout/finalizar` | User | Create order from cart |
| POST | `/pedidos/{id}/pagar` | User | Simulate payment |
| POST | `/pedidos/{id}/cancelar` | User | Cancel order |
| GET | `/pedidos/stream` | User | SSE stream — real-time order status updates |

### Admin — Orders

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/admin/pedidos` | Admin | List all orders |
| GET | `/admin/pedidos/status/{status}` | Admin | Filter orders by status |
| GET | `/admin/pedidos/{id}` | Admin | Order detail |
| POST | `/admin/pedidos/{id}/pagar` | Admin | Mark as paid (fires email + RabbitMQ) |
| POST | `/admin/pedidos/{id}/enviar` | Admin | Mark as shipped (fires email + RabbitMQ) |
| POST | `/admin/pedidos/{id}/entregar` | Admin | Mark as delivered |
| POST | `/admin/pedidos/{id}/cancelar` | Admin | Cancel order |

### Admin — DLQ

Endpoints to reprocess messages that failed in the RabbitMQ consumer and were routed to a Dead Letter Queue.

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/admin/dlq/queues` | Admin | List all DLQs |
| POST | `/admin/dlq/requeue/{queue}` | Admin | Requeue all messages from a specific DLQ |
| POST | `/admin/dlq/requeue-all` | Admin | Requeue all messages from all DLQs |

Valid `{queue}` values: `pedidos.criado.dlq`, `pedidos.pago.dlq`, `pedidos.enviado.dlq`, `pedidos.cancelado.dlq`

Full interactive docs: http://localhost:8080/api/swagger-ui.html

---

## Database

Managed by Flyway — never modify applied migrations, always create a new versioned file.

| Table | Description |
|---|---|
| `usuarios` | Users and roles |
| `categorias` | Product categories |
| `produtos` | Product catalogue |
| `estoques` | Per-product stock levels |
| `carrinhos` | User shopping carts |
| `itens_carrinho` | Cart line items |
| `pedidos` | Orders |
| `itens_pedido` | Order line items |
| `pagamentos` | Payment records |
| `entregas` | Delivery records |
| `notificacoes` | In-app notifications |

---

## Available Scripts

```bash
# Run (dev profile — H2, no Docker needed)
./mvnw spring-boot:run

# Run (with PostgreSQL + RabbitMQ from Docker)
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Tests
./mvnw test
./mvnw test -Dtest=ClassName          # Single test class
./mvnw verify                         # Tests + coverage

# Build
./mvnw clean package                  # Build JAR
./mvnw clean package -DskipTests      # Skip tests

# Docker
docker compose up -d                  # Start PostgreSQL + RabbitMQ + Mailhog
docker compose down                   # Stop all
docker compose logs -f                # Stream logs
docker compose logs rabbitmq          # RabbitMQ logs only
```

---

## Email Testing (Mailhog)

All dev emails are captured by Mailhog — no real emails sent.

1. Start: `docker compose up -d`
2. Open: http://localhost:8025
3. Trigger an order status change via Swagger
4. Email appears instantly in Mailhog UI

---

## Real-time Order Updates (SSE)

The backend exposes a Server-Sent Events stream that pushes order status changes to the authenticated user in real time, eliminating the need for polling.

**Endpoint:** `GET /api/pedidos/stream` (requires JWT)

**Frontend usage:**
```javascript
const source = new EventSource('http://localhost:8080/api/pedidos/stream', {
  headers: { Authorization: 'Bearer ' + token }
});

source.addEventListener('pedido.pago',      e => console.log('Paid!',      JSON.parse(e.data)));
source.addEventListener('pedido.enviado',   e => console.log('Shipped!',   JSON.parse(e.data)));
source.addEventListener('pedido.cancelado', e => console.log('Cancelled!', JSON.parse(e.data)));
source.addEventListener('pedido.criado',    e => console.log('Created!',   JSON.parse(e.data)));

// Reconnect on close (timeout after 5 min)
source.onerror = () => source.close();
```

Events are triggered automatically when `PedidoRabbitMQConsumer` processes a message. Each event payload is the corresponding `Pedido*Message` DTO serialized as JSON.

---

## Security

### Rate Limiting

`/auth/login` and `/auth/register` are protected against brute-force attacks via **Bucket4j** in-memory rate limiting:

- **Limit:** 10 requests per minute per IP address
- **Response on breach:** `429 Too Many Requests` with `Retry-After: 60` header
- **Implementation:** `RateLimitingFilter` (registered before JWT filter in `SecurityConfig`)

For multi-instance deployments, migrate `RateLimitingFilter` to use a shared store (Redis + Bucket4j Redis integration).

---

## RabbitMQ Management UI

1. Start: `docker compose up -d`
2. Open: http://localhost:15672 (user: `guest`, pass: `guest`)
3. Navigate to Queues to inspect `pedidos.criado`, `pedidos.pago`, etc.
4. Dead Letter Queues (`*.dlq`) hold failed messages for inspection/retry
5. Use the admin requeue API to reprocess failed messages (see [Admin — DLQ](#admin--dlq))

---

## Contributor & Agent Guidelines

- See [`AGENTS.md`](./AGENTS.md) for all coding rules, patterns, and conventions.
- See [`NEXT_STEPS.md`](./NEXT_STEPS.md) for pending work and known issues.
- See [`LAST_CHANGES.md`](./LAST_CHANGES.md) for a session-by-session changelog.

Key rules at a glance:
- Always run `./mvnw test` before committing
- Never put business logic in controllers
- Always emit domain events from entity methods, publish from use cases
- Publish events AFTER saving: `eventPublisher.publishAll()` then `limparEventos()`
- Never commit `.env`, `application-prod.properties`, or any secrets
- Never modify already-applied Flyway migrations

---

## Deployment

| Target | Platform | Notes |
|---|---|---|
| Backend | Railway | Auto-deploy from `main`; PostgreSQL + RabbitMQ managed by Railway |
| Frontend | Vercel | Separate repo at `minhavenda-frontend` |

Production build:
```bash
./mvnw clean package -DskipTests
# Produces: target/minhavenda-1.0.0.jar
```

Docker image:
```bash
docker build -t minhavenda-backend .
docker run -p 8080:8080 --env-file .env minhavenda-backend
```