package br.com.minhavenda.minhavenda.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração das filas, exchanges e bindings do RabbitMQ.
 *
 * Topologia:
 *   Exchange principal: pedidos.exchange  (TopicExchange)
 *   Dead Letter Exchange: pedidos.dlx     (DirectExchange)
 *
 *   Filas de negócio:
 *     pedidos.criado    ← routing key: pedido.criado
 *     pedidos.pago      ← routing key: pedido.pago
 *     pedidos.enviado   ← routing key: pedido.enviado
 *     pedidos.cancelado ← routing key: pedido.cancelado
 *
 *   Dead Letter Queues:
 *     pedidos.criado.dlq
 *     pedidos.pago.dlq
 *     pedidos.enviado.dlq
 *     pedidos.cancelado.dlq
 */
@Configuration
public class RabbitMQConfig {

    // =========================================================================
    // CONSTANTES
    // =========================================================================

    public static final String PEDIDOS_EXCHANGE     = "pedidos.exchange";
    public static final String PEDIDOS_DLX          = "pedidos.dlx";

    public static final String QUEUE_PEDIDO_CRIADO    = "pedidos.criado";
    public static final String QUEUE_PEDIDO_PAGO      = "pedidos.pago";
    public static final String QUEUE_PEDIDO_ENVIADO   = "pedidos.enviado";
    public static final String QUEUE_PEDIDO_CANCELADO = "pedidos.cancelado";

    public static final String DLQ_PEDIDO_CRIADO    = "pedidos.criado.dlq";
    public static final String DLQ_PEDIDO_PAGO      = "pedidos.pago.dlq";
    public static final String DLQ_PEDIDO_ENVIADO   = "pedidos.enviado.dlq";
    public static final String DLQ_PEDIDO_CANCELADO = "pedidos.cancelado.dlq";

    public static final String RK_PEDIDO_CRIADO    = "pedido.criado";
    public static final String RK_PEDIDO_PAGO      = "pedido.pago";
    public static final String RK_PEDIDO_ENVIADO   = "pedido.enviado";
    public static final String RK_PEDIDO_CANCELADO = "pedido.cancelado";

    // =========================================================================
    // EXCHANGES
    // =========================================================================

    @Bean
    public TopicExchange pedidosExchange() {
        return ExchangeBuilder.topicExchange(PEDIDOS_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange pedidosDlx() {
        return ExchangeBuilder.directExchange(PEDIDOS_DLX)
                .durable(true)
                .build();
    }

    // =========================================================================
    // FILAS DE NEGÓCIO (com DLQ configurada)
    // =========================================================================

    @Bean
    public Queue queuePedidoCriado() {
        return QueueBuilder.durable(QUEUE_PEDIDO_CRIADO)
                .withArgument("x-dead-letter-exchange", PEDIDOS_DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_PEDIDO_CRIADO)
                .build();
    }

    @Bean
    public Queue queuePedidoPago() {
        return QueueBuilder.durable(QUEUE_PEDIDO_PAGO)
                .withArgument("x-dead-letter-exchange", PEDIDOS_DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_PEDIDO_PAGO)
                .build();
    }

    @Bean
    public Queue queuePedidoEnviado() {
        return QueueBuilder.durable(QUEUE_PEDIDO_ENVIADO)
                .withArgument("x-dead-letter-exchange", PEDIDOS_DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_PEDIDO_ENVIADO)
                .build();
    }

    @Bean
    public Queue queuePedidoCancelado() {
        return QueueBuilder.durable(QUEUE_PEDIDO_CANCELADO)
                .withArgument("x-dead-letter-exchange", PEDIDOS_DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_PEDIDO_CANCELADO)
                .build();
    }

    // =========================================================================
    // DEAD LETTER QUEUES
    // =========================================================================

    @Bean
    public Queue dlqPedidoCriado() {
        return QueueBuilder.durable(DLQ_PEDIDO_CRIADO).build();
    }

    @Bean
    public Queue dlqPedidoPago() {
        return QueueBuilder.durable(DLQ_PEDIDO_PAGO).build();
    }

    @Bean
    public Queue dlqPedidoEnviado() {
        return QueueBuilder.durable(DLQ_PEDIDO_ENVIADO).build();
    }

    @Bean
    public Queue dlqPedidoCancelado() {
        return QueueBuilder.durable(DLQ_PEDIDO_CANCELADO).build();
    }

    // =========================================================================
    // BINDINGS — Exchange principal → Filas de negócio
    // =========================================================================

    @Bean
    public Binding bindingPedidoCriado() {
        return BindingBuilder.bind(queuePedidoCriado())
                .to(pedidosExchange())
                .with(RK_PEDIDO_CRIADO);
    }

    @Bean
    public Binding bindingPedidoPago() {
        return BindingBuilder.bind(queuePedidoPago())
                .to(pedidosExchange())
                .with(RK_PEDIDO_PAGO);
    }

    @Bean
    public Binding bindingPedidoEnviado() {
        return BindingBuilder.bind(queuePedidoEnviado())
                .to(pedidosExchange())
                .with(RK_PEDIDO_ENVIADO);
    }

    @Bean
    public Binding bindingPedidoCancelado() {
        return BindingBuilder.bind(queuePedidoCancelado())
                .to(pedidosExchange())
                .with(RK_PEDIDO_CANCELADO);
    }

    // =========================================================================
    // BINDINGS — DLX → Dead Letter Queues
    // =========================================================================

    @Bean
    public Binding bindingDlqCriado() {
        return BindingBuilder.bind(dlqPedidoCriado())
                .to(pedidosDlx())
                .with(DLQ_PEDIDO_CRIADO);
    }

    @Bean
    public Binding bindingDlqPago() {
        return BindingBuilder.bind(dlqPedidoPago())
                .to(pedidosDlx())
                .with(DLQ_PEDIDO_PAGO);
    }

    @Bean
    public Binding bindingDlqEnviado() {
        return BindingBuilder.bind(dlqPedidoEnviado())
                .to(pedidosDlx())
                .with(DLQ_PEDIDO_ENVIADO);
    }

    @Bean
    public Binding bindingDlqCancelado() {
        return BindingBuilder.bind(dlqPedidoCancelado())
                .to(pedidosDlx())
                .with(DLQ_PEDIDO_CANCELADO);
    }

    // =========================================================================
    // SERIALIZAÇÃO JSON
    // =========================================================================

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

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonMessageConverter());
        return factory;
    }
}
