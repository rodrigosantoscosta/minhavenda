# RabbitMQ - Implementação MinhaVenda

## Arquitetura Adotada: Bridge Pattern (Spring Events → RabbitMQ)

### Estratégia
O `PedidoEventListener` (que já existe) é enriquecido com um `PedidoRabbitMQProducer`.
O listener existente continua chamando email/notificação, E TAMBÉM publica no RabbitMQ.

## Estrutura de Filas
- Exchange: `pedidos.exchange` (topic)
- Filas:
  - `pedidos.criado` (routing key: `pedido.criado`)
  - `pedidos.pago`   (routing key: `pedido.pago`)
  - `pedidos.enviado` (routing key: `pedido.enviado`)
  - `pedidos.cancelado` (routing key: `pedido.cancelado`)
- Dead Letter Exchange: `pedidos.dlx`
- Dead Letter Queues: `pedidos.criado.dlq`, etc.

## Arquivos criados
- `infrastructure/messaging/producer/PedidoRabbitMQProducer.java` → Producer
- `infrastructure/messaging/consumer/PedidoRabbitMQConsumer.java` → Consumer
- `infrastructure/messaging/dto/PedidoCriadoMessage.java` etc. → Message DTOs
- `infrastructure/config/RabbitMQConfig.java` → Configuração filas/exchanges
- `application/event/DomainEventPublisher.java` → atualizado para publicar no RabbitMQ tbm

## application.yml RabbitMQ
```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASS:guest}
```
