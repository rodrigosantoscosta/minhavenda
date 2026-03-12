package br.com.minhavenda.minhavenda.infrastructure.messaging.consumer;

import br.com.minhavenda.minhavenda.infrastructure.config.RabbitMQConfig;
import br.com.minhavenda.minhavenda.infrastructure.messaging.dto.PedidoCanceladoMessage;
import br.com.minhavenda.minhavenda.infrastructure.messaging.dto.PedidoCriadoMessage;
import br.com.minhavenda.minhavenda.infrastructure.messaging.dto.PedidoEnviadoMessage;
import br.com.minhavenda.minhavenda.infrastructure.messaging.dto.PedidoPagoMessage;
import br.com.minhavenda.minhavenda.infrastructure.sse.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoRabbitMQConsumer {

    private final SseEmitterRegistry sseRegistry;

    // =========================================================================
    // PEDIDO CRIADO
    // =========================================================================

    /**
     * Processa o evento de pedido criado.
     *
     * Casos de uso tipicos:
     * - Registrar no sistema de analytics
     * - Reservar estoque no WMS
     * - Notificar sistemas de antifraude
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_PEDIDO_CRIADO)
    public void onPedidoCriado(PedidoCriadoMessage message) {
        log.info("📥 [RabbitMQ] PedidoCriado recebido — pedidoId: {} | usuario: {} | total: R$ {}",
                message.getPedidoId(), message.getEmailUsuario(), message.getValorTotal());
        try {
            // TODO: integrar com sistema de analytics / WMS / antifraude
            log.info("✅ [RabbitMQ] PedidoCriado processado — pedidoId: {}", message.getPedidoId());
            sseRegistry.sendEvent(message.getUsuarioId(), "pedido.criado", message);
        } catch (Exception e) {
            log.error("❌ [RabbitMQ] Erro ao processar PedidoCriado — pedidoId: {} | erro: {}",
                    message.getPedidoId(), e.getMessage(), e);
            throw e;
        }
    }

    // =========================================================================
    // PEDIDO PAGO
    // =========================================================================

    /**
     * Processa o evento de pedido pago.
     *
     * Casos de uso tipicos:
     * - Confirmar separacao no WMS/fulfillment
     * - Atualizar ERP financeiro
     * - Emitir nota fiscal via integracao
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_PEDIDO_PAGO)
    public void onPedidoPago(PedidoPagoMessage message) {
        log.info("📥 [RabbitMQ] PedidoPago recebido — pedidoId: {} | método: {} | valor: R$ {}",
                message.getPedidoId(), message.getMetodoPagamento(), message.getValorPago());
        try {
            // TODO: integrar com WMS (confirmar separacao), ERP (financeiro), NF-e
            log.info("✅ [RabbitMQ] PedidoPago processado — pedidoId: {}", message.getPedidoId());
            sseRegistry.sendEvent(message.getUsuarioId(), "pedido.pago", message);
        } catch (Exception e) {
            log.error("❌ [RabbitMQ] Erro ao processar PedidoPago — pedidoId: {} | erro: {}",
                    message.getPedidoId(), e.getMessage(), e);
            throw e;
        }
    }

    // =========================================================================
    // PEDIDO ENVIADO
    // =========================================================================

    /**
     * Processa o evento de pedido enviado.
     *
     * Casos de uso tipicos:
     * - Registrar codigo de rastreio no sistema de logistica
     * - Notificar transportadora via API
     * - Atualizar dashboard de acompanhamento
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_PEDIDO_ENVIADO)
    public void onPedidoEnviado(PedidoEnviadoMessage message) {
        log.info("📥 [RabbitMQ] PedidoEnviado recebido — pedidoId: {} | rastreio: {} | transportadora: {}",
                message.getPedidoId(), message.getCodigoRastreio(), message.getTransportadora());
        try {
            // TODO: integrar com API da transportadora, dashboard de rastreio
            log.info("✅ [RabbitMQ] PedidoEnviado processado — pedidoId: {}", message.getPedidoId());
            sseRegistry.sendEvent(message.getUsuarioId(), "pedido.enviado", message);
        } catch (Exception e) {
            log.error("❌ [RabbitMQ] Erro ao processar PedidoEnviado — pedidoId: {} | erro: {}",
                    message.getPedidoId(), e.getMessage(), e);
            throw e;
        }
    }

    // =========================================================================
    // PEDIDO CANCELADO
    // =========================================================================

    /**
     * Processa o evento de pedido cancelado.
     *
     * Casos de uso tipicos:
     * - Estornar reserva de estoque
     * - Iniciar processo de reembolso
     * - Atualizar relatorios de cancelamento
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_PEDIDO_CANCELADO)
    public void onPedidoCancelado(PedidoCanceladoMessage message) {
        log.info("📥 [RabbitMQ] PedidoCancelado recebido — pedidoId: {} | motivo: {}",
                message.getPedidoId(), message.getMotivo());
        try {
            // TODO: estornar estoque, iniciar reembolso, atualizar relatorios
            log.info("✅ [RabbitMQ] PedidoCancelado processado — pedidoId: {}", message.getPedidoId());
            sseRegistry.sendEvent(message.getUsuarioId(), "pedido.cancelado", message);
        } catch (Exception e) {
            log.error("❌ [RabbitMQ] Erro ao processar PedidoCancelado — pedidoId: {} | erro: {}",
                    message.getPedidoId(), e.getMessage(), e);
            throw e;
        }
    }
}
