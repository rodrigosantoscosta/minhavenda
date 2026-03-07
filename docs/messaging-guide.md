# Guia de Mensageria — MinhaVenda 📨

> **Formato:** Guia de estudo — explica o **porquê**, o **como** e o **o que** de cada peça da implementação.

---

## 1. Contexto: Por que mensageria?

Num e-commerce, quando um pedido muda de estado (criado, pago, enviado, cancelado), vários sistemas precisam reagir:

| Sistema | Precisa saber quando... |
|---|---|
| E-mail | Pedido criado, pago, enviado, cancelado |
| Notificação in-app | Qualquer mudança de estado |
| WMS (estoque/separação) | Pedido criado ou pago |
| ERP financeiro | Pedido pago |
| NF-e | Pedido pago |
| Transportadora | Pedido enviado |
| Analytics | Qualquer evento |

Se a lógica de negócio chamar todos esses sistemas diretamente (de forma síncrona), o código fica acoplado, lento e frágil. **RabbitMQ resolve isso** desacoplando quem produz o evento de quem o consome.

---

## 2. Arquitetura Adotada: Bridge Pattern

```
┌─────────────────────────────────────────────────────────────┐
│                      DOMÍNIO (Java)                         │
│                                                             │
│  UseCase → publica → ApplicationEventPublisher              │
│                              │                              │
│                              ▼                              │
│                    PedidoEventListener  ◄── @EventListener  │
│                    (Spring Events)                          │
│                    │         │         │                    │
│                    ▼         ▼         ▼                    │
│               EmailSvc  NotifSvc  RabbitMQProducer          │
└─────────────────────────────────────────────────────────────┘
                                   │
                          publica JSON na
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────┐
│                        RABBITMQ                             │
│                                                             │
│   pedidos.exchange (TopicExchange)                          │
│   ├── pedido.criado    ──► pedidos.criado    ──► [DLQ]      │
│   ├── pedido.pago      ──► pedidos.pago      ──► [DLQ]      │
│   ├── pedido.enviado   ──► pedidos.enviado   ──► [DLQ]      │
│   └── pedido.cancelado ──► pedidos.cancelado ──► [DLQ]      │
└─────────────────────────────────────────────────────────────┘
                                   │
                          consome mensagens
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────┐
│                   PedidoRabbitMQConsumer                    │
│   @RabbitListener por fila — processa cada evento           │
└─────────────────────────────────────────────────────────────┘
```

**Por que o Bridge Pattern?**  
O listener de Spring Events (`PedidoEventListener`) já existia para e-mail e notificações. Em vez de refatorar tudo, adicionamos o `PedidoRabbitMQProducer` como um terceiro destino dentro do mesmo listener. O domínio não sabe nada sobre RabbitMQ — só publica um evento Java, e a infraestrutura cuida do resto.

---

## 3. Topologia do RabbitMQ

### 3.1 Exchanges

| Nome | Tipo | Finalidade |
|---|---|---|
| `pedidos.exchange` | `TopicExchange` | Recebe todos os eventos de pedido |
| `pedidos.dlx` | `DirectExchange` | Recebe mensagens que falharam |

**Por que TopicExchange?**  
Topic exchanges usam routing keys com padrões (wildcards `*` e `#`). Isso permite, no futuro, ter um consumer que escuta `pedido.*` (todos os eventos) ou `pedido.pago` (só pagamentos) sem alterar o producer.

### 3.2 Filas de Negócio

| Fila | Routing Key | Evento |
|---|---|---|
| `pedidos.criado` | `pedido.criado` | Pedido foi criado no checkout |
| `pedidos.pago` | `pedido.pago` | Pagamento foi confirmado |
| `pedidos.enviado` | `pedido.enviado` | Pedido saiu para entrega |
| `pedidos.cancelado` | `pedido.cancelado` | Pedido foi cancelado |

### 3.3 Dead Letter Queues (DLQ)

Cada fila de negócio tem uma DLQ correspondente:

```
pedidos.criado    → falha → pedidos.dlx → pedidos.criado.dlq
pedidos.pago      → falha → pedidos.dlx → pedidos.pago.dlq
pedidos.enviado   → falha → pedidos.dlx → pedidos.enviado.dlq
pedidos.cancelado → falha → pedidos.dlx → pedidos.cancelado.dlq
```

**O que é uma DLQ?**  
Quando um consumer lança uma exceção e não consegue processar a mensagem, o RabbitMQ não a perde — ele a redireciona para a DLQ. Isso permite investigar e reprocessar mensagens problemáticas sem perder dados.

A configuração é feita por argumentos na fila:
```java
QueueBuilder.durable(QUEUE_PEDIDO_CRIADO)
    .withArgument("x-dead-letter-exchange", PEDIDOS_DLX)
    .withArgument("x-dead-letter-routing-key", DLQ_PEDIDO_CRIADO)
    .build();
```

---

## 4. Configuração (`RabbitMQConfig.java`)

Este arquivo é o coração da topologia. Ele declara **Beans Spring** que o RabbitMQ Admin usará para criar automaticamente todas as estruturas ao iniciar a aplicação.

```java
// Exchange principal — topic para suportar wildcards no futuro
@Bean
public TopicExchange pedidosExchange() {
    return ExchangeBuilder.topicExchange("pedidos.exchange")
            .durable(true)  // sobrevive a restart do broker
            .build();
}

// Fila com DLQ configurada
@Bean
public Queue queuePedidoCriado() {
    return QueueBuilder.durable("pedidos.criado")
            .withArgument("x-dead-letter-exchange", "pedidos.dlx")
            .withArgument("x-dead-letter-routing-key", "pedidos.criado.dlq")
            .build();
}

// Binding: liga a fila à exchange pela routing key
@Bean
public Binding bindingPedidoCriado() {
    return BindingBuilder.bind(queuePedidoCriado())
            .to(pedidosExchange())
            .with("pedido.criado");  // routing key
}
```

### Serialização JSON

```java
@Bean
public MessageConverter jacksonMessageConverter() {
    return new Jackson2JsonMessageConverter();
}

@Bean
public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(jacksonMessageConverter());
    return template;
}
```

Sem isso, o RabbitMQ serializa mensagens como bytes Java (não legível). Com o `Jackson2JsonMessageConverter`, as mensagens viajam como JSON puro — legível no Management UI e interoperável com outros sistemas.

---

## 5. Message DTOs

Cada evento tem seu próprio DTO de mensagem, separado do Domain Event. Isso é intencional:

```
Domain Event (interno)          Message DTO (externo/RabbitMQ)
PedidoCriadoEvent      →map→    PedidoCriadoMessage
```

**Por que separar?**  
- O Domain Event pode ter referências a objetos de domínio complexos
- O Message DTO é um contrato de API — só tipos primitivos e serializáveis
- Mudanças no domínio não quebram o contrato de mensageria

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PedidoCriadoMessage {
    private UUID eventId;        // ID único do evento (para idempotência)
    private UUID pedidoId;       // ID do pedido
    private UUID usuarioId;      // ID do usuário
    private String emailUsuario;
    private String nomeUsuario;
    private Double valorTotal;
    private Integer quantidadeItens;
    private Instant ocorridoEm;  // timestamp do evento
}
```

O campo `eventId` é importante para **idempotência** — se a mesma mensagem for entregue duas vezes (o RabbitMQ garante *at-least-once delivery*), o consumer pode checar se já processou esse `eventId`.

---

## 6. Producer (`PedidoRabbitMQProducer.java`)

O producer mapeia Domain Events para Message DTOs e os envia para a exchange:

```java
public void publicarPedidoCriado(PedidoCriadoEvent event) {
    // 1. Mapeia domain event → message DTO
    PedidoCriadoMessage message = PedidoCriadoMessage.builder()
            .eventId(event.getEventId())
            .pedidoId(event.getPedidoId())
            // ... demais campos
            .ocorridoEm(Instant.now())
            .build();

    // 2. Publica na exchange com a routing key correta
    rabbitTemplate.convertAndSend(
        "pedidos.exchange",   // exchange
        "pedido.criado",      // routing key
        message               // serializado como JSON
    );
}
```

**Decisão importante:** o producer **não relança exceções**:

```java
private void publicar(...) {
    try {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        log.info("📤 [RabbitMQ] {} publicado...", tipoEvento, pedidoId);
    } catch (Exception e) {
        log.error("❌ [RabbitMQ] Falha ao publicar...", e);
        // NÃO relança — falha no RabbitMQ não deve impedir e-mail/notificação
    }
}
```

Isso garante que, se o RabbitMQ estiver fora do ar, o e-mail e a notificação in-app ainda funcionam normalmente.

---

## 7. Event Listener (`PedidoEventListener.java`)

Este é o ponto de integração entre o domínio e a infraestrutura:

```java
@Async           // executa em thread separada — não bloqueia o use case
@EventListener   // escuta eventos Spring publicados via ApplicationEventPublisher
public void handlePedidoCriado(PedidoCriadoEvent event) {
    try {
        // 1. E-mail de confirmação
        emailService.enviarEmailPedidoCriado(...);

        // 2. Notificação in-app
        notificationService.criarNotificacao(...);

        // 3. RabbitMQ — bridge para sistemas externos
        rabbitMQProducer.publicarPedidoCriado(event);

    } catch (Exception e) {
        log.error("❌ Erro ao processar PedidoCriadoEvent...", e);
    }
}
```

**`@Async` é fundamental aqui.** O use case de checkout não precisa esperar o e-mail ser enviado e a mensagem publicada para retornar a resposta HTTP. O listener roda em background.

---

## 8. Consumer (`PedidoRabbitMQConsumer.java`)

O consumer escuta cada fila e processa as mensagens:

```java
@RabbitListener(queues = "pedidos.criado")
public void onPedidoCriado(PedidoCriadoMessage message) {
    log.info("📥 PedidoCriado recebido — pedidoId: {}", message.getPedidoId());
    try {
        // lógica de negócio: analytics, WMS, antifraude...
        log.info("✅ PedidoCriado processado — pedidoId: {}", message.getPedidoId());
    } catch (Exception e) {
        log.error("❌ Erro ao processar PedidoCriado...", e);
        throw e; // relança → mensagem vai para a DLQ
    }
}
```

**Por que relançar a exceção aqui?** Ao contrário do producer, o consumer **deve** relançar. Se ocorrer um erro, queremos que o Spring AMQP saiba que o processamento falhou e encaminhe a mensagem para a DLQ, em vez de descartá-la silenciosamente.

---

## 9. Fluxo Completo — Exemplo: Checkout

```
1. POST /pedidos/checkout
        │
        ▼
2. CheckoutUseCase executa regras de negócio
        │
        ▼
3. ApplicationEventPublisher.publishEvent(new PedidoCriadoEvent(...))
        │
        ▼ (assíncrono — @Async)
4. PedidoEventListener.handlePedidoCriado()
        ├── emailService.enviarEmailPedidoCriado()
        ├── notificationService.criarNotificacao()
        └── rabbitMQProducer.publicarPedidoCriado()
                │
                ▼
5. RabbitMQ recebe JSON em pedidos.exchange com RK "pedido.criado"
        │
        ▼ (roteado pelo binding)
6. Mensagem chega na fila pedidos.criado
        │
        ▼
7. PedidoRabbitMQConsumer.onPedidoCriado() processa
        ├── sucesso → mensagem removida da fila
        └── falha   → mensagem vai para pedidos.criado.dlq
```

---

## 10. Configuração no `application.properties`

```properties
# Conexão com o broker
spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USER:guest}
spring.rabbitmq.password=${RABBITMQ_PASS:guest}
spring.rabbitmq.virtual-host=/

# Retry automático (3 tentativas antes de ir para DLQ)
spring.rabbitmq.listener.simple.retry.enabled=true
spring.rabbitmq.listener.simple.retry.initial-interval=2000
spring.rabbitmq.listener.simple.retry.max-attempts=3
spring.rabbitmq.listener.simple.retry.multiplier=2

# Acknowledge automático (Spring gerencia o ack)
spring.rabbitmq.listener.simple.acknowledge-mode=auto
```

**O que é o retry?** Antes de mover uma mensagem para a DLQ, o Spring AMQP tenta reprocessá-la 3 vezes com intervalo crescente (2s, 4s, 8s). Só depois de todas as tentativas falharem a mensagem vai para a DLQ.

---

## 11. Como Verificar no RabbitMQ Management UI

Acesse: **http://localhost:15672** — login: `guest` / `guest`

| Aba | O que ver |
|---|---|
| **Exchanges** | `pedidos.exchange` e `pedidos.dlx` criados |
| **Queues** | 8 filas (4 de negócio + 4 DLQs) |
| **Queues → pedidos.criado** | Gráfico de taxa de mensagens ao fazer um checkout |
| **Queues → pedidos.criado → Get messages** | Ver o JSON da mensagem |

---

## 12. Extensibilidade — O que pode ser implementado nos TODOs

Os consumers têm `// TODO` marcando onde a lógica de integração vai:

| Consumer | Integrações sugeridas |
|---|---|
| `onPedidoCriado` | Analytics, reserva de estoque no WMS, antifraude |
| `onPedidoPago` | Emissão de NF-e, ERP financeiro, WMS (iniciar separação) |
| `onPedidoEnviado` | API da transportadora, dashboard de rastreio |
| `onPedidoCancelado` | Estorno de estoque, processo de reembolso, relatórios |

Para adicionar uma nova integração, basta implementar a lógica dentro do método `try` do consumer correspondente — sem alterar producer, exchange, filas ou domínio.

---

## 13. Dependências Maven

```xml
<!-- Spring Boot AMQP (RabbitMQ) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>

<!-- Jackson para serialização JSON das mensagens -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

---

*Gerado em março/2026 — MinhaVenda Messaging Guide*
