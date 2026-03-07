package br.com.minhavenda.minhavenda.infrastructure.messaging.consumer;

import br.com.minhavenda.minhavenda.infrastructure.config.RabbitMQConfig;
import br.com.minhavenda.minhavenda.infrastructure.messaging.dto.PedidoCanceladoMessage;
import br.com.minhavenda.minhavenda.infrastructure.messaging.dto.PedidoCriadoMessage;
import br.com.minhavenda.minhavenda.infrastructure.messaging.dto.PedidoEnviadoMessage;
import br.com.minhavenda.minhavenda.infrastructure.messaging.dto.PedidoPagoMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer responsável por processar mensagens de pedido vindas do RabbitMQ.
 *
 * Cada método escuta uma fila dedicada e executa a lógica de negócio
 * correspondente ao evento recebido (ex: analytics, notificações externas,
 * integrações com ERP/WMS, etc.).
 *
 * Em caso de exceção não tratada, a mensagem é automaticamente
 * encaminhada para a Dead Letter Queue configurada em {@link RabbitMQConfig}.
 *
 * Filas ouvidas:
 *   pedidos.criado    → onPedidoCriado
 *   pedidos.pago      → onPedidoPago
 *   pedidos.enviado   → onPedidoEnviado
 *   pedidos.cancelado → onPedidoCancelado
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoRabbitMQConsumer {

    // =========================================================================
    // PEDIDO CRIADO
    // =========================================================================

    /**
     * Processa o evento de pedido criado.
     *
     * Casos de uso típicos:
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
        } catch (Exception e) {
            log.error("❌ [RabbitMQ] Erro ao processar PedidoCriado — pedidoId: {} | erro: {}",
                    message.getPedidoId(), e.getMessage(), e);
            throw e; // Relança para que o Spring AMQP encaminhe para a DLQ
        }
    }

    // =========================================================================
    // PEDIDO PAGO
    // =========================================================================

    /**
     * Processa o evento de pedido pago.
     *
     * Casos de uso típicos:
     * - Confirmar separação no WMS/fulfillment
     * - Atualizar ERP financeiro
     * - Emitir nota fiscal via integração
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_PEDIDO_PAGO)
    public void onPedidoPago(PedidoPagoMessage message) {
        log.info("📥 [RabbitMQ] PedidoPago recebido — pedidoId: {} | método: {} | valor: R$ {}",
                message.getPedidoId(), message.getMetodoPagamento(), message.getValorPago());
        try {
            // TODO: integrar com WMS (confirmar separação), ERP (financeiro), NF-e
            log.info("✅ [RabbitMQ] PedidoPago processado — pedidoId: {}", message.getPedidoId());
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
     * Casos de uso típicos:
     * - Registrar código de rastreio no sistema de logística
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
     * Casos de uso típicos:
     * - Estornar reserva de estoque
     * - Iniciar processo de reembolso
     * - Atualizar relatórios de cancelamento
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_PEDIDO_CANCELADO)
    public void onPedidoCancelado(PedidoCanceladoMessage message) {
        log.info("📥 [RabbitMQ] PedidoCancelado recebido — pedidoId: {} | motivo: {}",
                message.getPedidoId(), message.getMotivo());
        try {
            // TODO: estornar estoque, iniciar reembolso, atualizar relatórios
            log.info("✅ [RabbitMQ] PedidoCancelado processado — pedidoId: {}", message.getPedidoId());
        } catch (Exception e) {
            log.error("❌ [RabbitMQ] Erro ao processar PedidoCancelado — pedidoId: {} | erro: {}",
                    message.getPedidoId(), e.getMessage(), e);
            throw e;
        }
    }
}
