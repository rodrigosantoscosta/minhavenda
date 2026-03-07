package br.com.minhavenda.minhavenda.infrastructure.event.listener;

import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoCriadoEvent;
import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoPagoEvent;
import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoEnviadoEvent;
import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoCanceladoEvent;
import br.com.minhavenda.minhavenda.infrastructure.messaging.producer.PedidoRabbitMQProducer;
import br.com.minhavenda.minhavenda.infrastructure.notification.EmailService;
import br.com.minhavenda.minhavenda.infrastructure.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoEventListener {

    private final EmailService emailService;
    private final NotificationService notificationService;
    private final PedidoRabbitMQProducer rabbitMQProducer;

    // ========================================================================
    // PEDIDO CRIADO
    // ========================================================================

    /**
     * Quando pedido é criado:
     *  1. Envia e-mail de confirmação
     *  2. Cria notificação in-app
     *  3. Publica evento no RabbitMQ (para integração com WMS, analytics, etc.)
     */
    @Async
    @EventListener
    public void handlePedidoCriado(PedidoCriadoEvent event) {
        log.info("🎧 Processando PedidoCriadoEvent [{}] - Pedido: {}",
                event.getEventId(), event.getPedidoId());

        try {
            emailService.enviarEmailPedidoCriado(
                    event.getEmailUsuario(),
                    event.getNomeUsuario(),
                    event.getPedidoId(),
                    event.getValorTotal(),
                    event.getQuantidadeItens()
            );

            notificationService.criarNotificacao(
                    event.getUsuarioId(),
                    "✅ Pedido Criado",
                    String.format("Seu pedido #%s foi criado com sucesso! Total: R$ %.2f",
                            event.getPedidoId().toString().substring(0, 8),
                            event.getValorTotal())
            );

            rabbitMQProducer.publicarPedidoCriado(event);

            log.info("✅ PedidoCriadoEvent processado com sucesso: {}", event.getPedidoId());

        } catch (Exception e) {
            log.error("❌ Erro ao processar PedidoCriadoEvent [{}] - Pedido: {}",
                    event.getEventId(), event.getPedidoId(), e);
        }
    }

    // ========================================================================
    // PEDIDO PAGO
    // ========================================================================

    /**
     * Quando pedido é pago:
     *  1. Envia e-mail de confirmação de pagamento
     *  2. Cria notificação in-app
     *  3. Publica evento no RabbitMQ (para NF-e, WMS, ERP, etc.)
     */
    @Async
    @EventListener
    public void handlePedidoPago(PedidoPagoEvent event) {
        log.info("🎧 Processando PedidoPagoEvent [{}] - Pedido: {} - Método: {}",
                event.getEventId(), event.getPedidoId(), event.getMetodoPagamento());

        try {
            emailService.enviarEmailPedidoPago(
                    event.getEmailUsuario(),
                    event.getPedidoId(),
                    event.getValorPago(),
                    event.getMetodoPagamento()
            );

            notificationService.criarNotificacao(
                    event.getUsuarioId(),
                    "💰 Pagamento Confirmado",
                    String.format("Pagamento do pedido #%s confirmado via %s! Valor: R$ %.2f",
                            event.getPedidoId().toString().substring(0, 8),
                            event.getMetodoPagamento(),
                            event.getValorPago())
            );

            rabbitMQProducer.publicarPedidoPago(event);

            log.info("✅ PedidoPagoEvent processado com sucesso: {} - {}",
                    event.getPedidoId(), event.getMetodoPagamento());

        } catch (Exception e) {
            log.error("❌ Erro ao processar PedidoPagoEvent [{}] - Pedido: {}",
                    event.getEventId(), event.getPedidoId(), e);
        }
    }

    // ========================================================================
    // PEDIDO ENVIADO
    // ========================================================================

    /**
     * Quando pedido é enviado:
     *  1. Envia e-mail com código de rastreio
     *  2. Cria notificação in-app
     *  3. Publica evento no RabbitMQ (para transportadora, dashboard, etc.)
     */
    @Async
    @EventListener
    public void handlePedidoEnviado(PedidoEnviadoEvent event) {
        log.info("🎧 Processando PedidoEnviadoEvent [{}] - Pedido: {} - Rastreio: {}",
                event.getEventId(), event.getPedidoId(), event.getCodigoRastreio());

        try {
            emailService.enviarEmailPedidoEnviado(
                    event.getEmailUsuario(),
                    event.getNomeUsuario(),
                    event.getPedidoId(),
                    event.getCodigoRastreio(),
                    event.getTransportadora(),
                    event.getTelefone()
            );

            notificationService.criarNotificacao(
                    event.getUsuarioId(),
                    "📦 Pedido Enviado",
                    String.format("Seu pedido #%s foi enviado via %s! Código de rastreio: %s",
                            event.getPedidoId().toString().substring(0, 8),
                            event.getTransportadora(),
                            event.getCodigoRastreio())
            );

            rabbitMQProducer.publicarPedidoEnviado(event);

            log.info("✅ PedidoEnviadoEvent processado com sucesso: {} - Rastreio: {} - Transportadora: {}",
                    event.getPedidoId(), event.getCodigoRastreio(), event.getTransportadora());

        } catch (Exception e) {
            log.error("❌ Erro ao processar PedidoEnviadoEvent [{}] - Pedido: {}",
                    event.getEventId(), event.getPedidoId(), e);
        }
    }

    // ========================================================================
    // PEDIDO CANCELADO
    // ========================================================================

    /**
     * Quando pedido é cancelado:
     *  1. Envia e-mail de cancelamento
     *  2. Cria notificação in-app
     *  3. Publica evento no RabbitMQ (para estorno de estoque, reembolso, etc.)
     */
    @Async
    @EventListener
    public void handlePedidoCancelado(PedidoCanceladoEvent event) {
        log.info("🎧 Processando PedidoCanceladoEvent [{}] - Pedido: {} - Motivo: {}",
                event.getEventId(), event.getPedidoId(), event.getMotivo());

        try {
            emailService.enviarEmailPedidoCancelado(
                    event.getEmailUsuario(),
                    event.getPedidoId(),
                    event.getMotivo()
            );

            notificationService.criarNotificacao(
                    event.getUsuarioId(),
                    "❌ Pedido Cancelado",
                    String.format("Pedido #%s foi cancelado. Motivo: %s",
                            event.getPedidoId().toString().substring(0, 8),
                            event.getMotivo())
            );

            rabbitMQProducer.publicarPedidoCancelado(event);

            log.info("✅ PedidoCanceladoEvent processado com sucesso: {} - Motivo: {}",
                    event.getPedidoId(), event.getMotivo());

        } catch (Exception e) {
            log.error("❌ Erro ao processar PedidoCanceladoEvent [{}] - Pedido: {}",
                    event.getEventId(), event.getPedidoId(), e);
        }
    }
}
