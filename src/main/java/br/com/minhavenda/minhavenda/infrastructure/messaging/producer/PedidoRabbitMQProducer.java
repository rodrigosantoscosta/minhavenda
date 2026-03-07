package br.com.minhavenda.minhavenda.infrastructure.messaging.producer;

import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoCanceladoEvent;
import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoCriadoEvent;
import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoEnviadoEvent;
import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoPagoEvent;
import br.com.minhavenda.minhavenda.infrastructure.config.RabbitMQConfig;
import br.com.minhavenda.minhavenda.infrastructure.messaging.dto.PedidoCanceladoMessage;
import br.com.minhavenda.minhavenda.infrastructure.messaging.dto.PedidoCriadoMessage;
import br.com.minhavenda.minhavenda.infrastructure.messaging.dto.PedidoEnviadoMessage;
import br.com.minhavenda.minhavenda.infrastructure.messaging.dto.PedidoPagoMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Producer responsável por publicar eventos de pedido no RabbitMQ.
 *
 * Cada método mapeia um DomainEvent para um Message DTO e publica
 * na exchange {@code pedidos.exchange} com a routing key correspondente.
 *
 * Topologia:
 *   pedidos.exchange (topic)
 *     pedido.criado    → pedidos.criado
 *     pedido.pago      → pedidos.pago
 *     pedido.enviado   → pedidos.enviado
 *     pedido.cancelado → pedidos.cancelado
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoRabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    // =========================================================================
    // PEDIDO CRIADO
    // =========================================================================

    public void publicarPedidoCriado(PedidoCriadoEvent event) {
        PedidoCriadoMessage message = PedidoCriadoMessage.builder()
                .eventId(event.getEventId())
                .pedidoId(event.getPedidoId())
                .usuarioId(event.getUsuarioId())
                .emailUsuario(event.getEmailUsuario())
                .nomeUsuario(event.getNomeUsuario())
                .valorTotal(event.getValorTotal())
                .quantidadeItens(event.getQuantidadeItens())
                .ocorridoEm(Instant.now())
                .build();

        publicar(RabbitMQConfig.PEDIDOS_EXCHANGE, RabbitMQConfig.RK_PEDIDO_CRIADO, message,
                "PedidoCriado", event.getPedidoId());
    }

    // =========================================================================
    // PEDIDO PAGO
    // =========================================================================

    public void publicarPedidoPago(PedidoPagoEvent event) {
        PedidoPagoMessage message = PedidoPagoMessage.builder()
                .eventId(event.getEventId())
                .pedidoId(event.getPedidoId())
                .usuarioId(event.getUsuarioId())
                .emailUsuario(event.getEmailUsuario())
                .valorPago(event.getValorPago())
                .metodoPagamento(event.getMetodoPagamento())
                .ocorridoEm(Instant.now())
                .build();

        publicar(RabbitMQConfig.PEDIDOS_EXCHANGE, RabbitMQConfig.RK_PEDIDO_PAGO, message,
                "PedidoPago", event.getPedidoId());
    }

    // =========================================================================
    // PEDIDO ENVIADO
    // =========================================================================

    public void publicarPedidoEnviado(PedidoEnviadoEvent event) {
        PedidoEnviadoMessage message = PedidoEnviadoMessage.builder()
                .eventId(event.getEventId())
                .pedidoId(event.getPedidoId())
                .usuarioId(event.getUsuarioId())
                .nomeUsuario(event.getNomeUsuario())
                .emailUsuario(event.getEmailUsuario())
                .telefone(event.getTelefone())
                .codigoRastreio(event.getCodigoRastreio())
                .transportadora(event.getTransportadora())
                .ocorridoEm(Instant.now())
                .build();

        publicar(RabbitMQConfig.PEDIDOS_EXCHANGE, RabbitMQConfig.RK_PEDIDO_ENVIADO, message,
                "PedidoEnviado", event.getPedidoId());
    }

    // =========================================================================
    // PEDIDO CANCELADO
    // =========================================================================

    public void publicarPedidoCancelado(PedidoCanceladoEvent event) {
        PedidoCanceladoMessage message = PedidoCanceladoMessage.builder()
                .eventId(event.getEventId())
                .pedidoId(event.getPedidoId())
                .usuarioId(event.getUsuarioId())
                .emailUsuario(event.getEmailUsuario())
                .motivo(event.getMotivo())
                .ocorridoEm(Instant.now())
                .build();

        publicar(RabbitMQConfig.PEDIDOS_EXCHANGE, RabbitMQConfig.RK_PEDIDO_CANCELADO, message,
                "PedidoCancelado", event.getPedidoId());
    }

    // =========================================================================
    // HELPER INTERNO
    // =========================================================================

    private void publicar(String exchange, String routingKey, Object message,
                          String tipoEvento, UUID pedidoId) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("📤 [RabbitMQ] {} publicado — pedidoId: {} | exchange: {} | rk: {}",
                    tipoEvento, pedidoId, exchange, routingKey);
        } catch (Exception e) {
            log.error("❌ [RabbitMQ] Falha ao publicar {} — pedidoId: {} | erro: {}",
                    tipoEvento, pedidoId, e.getMessage(), e);
            // Não relançar: o EventListener trata o fluxo principal;
            // falhas no RabbitMQ não devem derrubar o processamento de e-mail/notificação.
        }
    }
}
