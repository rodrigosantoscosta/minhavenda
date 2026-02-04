package br.com.minhavenda.minhavenda.infrastructure.event.listener;

import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoCriadoEvent;
import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoPagoEvent;
import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoEnviadoEvent;
import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoCanceladoEvent;
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

    // ========================================================================
    // PEDIDO CRIADO
    // ========================================================================

    /**
     * Quando pedido é criado → Enviar email de confirmação
     *
     * Event disparado em: Pedido.registrarCriacao()
     * Campos disponíveis: pedidoId, usuarioId, emailUsuario, nomeUsuario, valorTotal, quantidadeItens
     */
    @Async
    @EventListener
    public void handlePedidoCriado(PedidoCriadoEvent event) {
        log.info("🎧 Processando PedidoCriadoEvent [{}] - Pedido: {}",
                event.getEventId(), event.getPedidoId());

        try {
            // Enviar email de confirmação
            emailService.enviarEmailPedidoCriado(
                    event.getEmailUsuario(),
                    event.getNomeUsuario(),
                    event.getPedidoId(),
                    event.getValorTotal(),
                    event.getQuantidadeItens()
            );

            // Criar notificação in-app
            notificationService.criarNotificacao(
                    event.getUsuarioId(),
                    "✅ Pedido Criado",
                    String.format("Seu pedido #%s foi criado com sucesso! Total: R$ %.2f",
                            event.getPedidoId().toString().substring(0, 8),
                            event.getValorTotal())
            );

            log.info("✅ PedidoCriadoEvent processado com sucesso: {}", event.getPedidoId());

        } catch (Exception e) {
            log.error("❌ Erro ao processar PedidoCriadoEvent [{}] - Pedido: {}",
                    event.getEventId(), event.getPedidoId(), e);
            // TODO: Enviar para Dead Letter Queue para retry
        }
    }

    // ========================================================================
    // PEDIDO PAGO
    // ========================================================================

    /**
     * Quando pedido é pago → Enviar email de confirmação de pagamento
     *
     * Event disparado em: Pedido.pagar(metodoPagamento)
     * Campos disponíveis: pedidoId, usuarioId, emailUsuario, valorTotal, metodoPagamento
     */
    @Async
    @EventListener
    public void handlePedidoPago(PedidoPagoEvent event) {
        log.info("🎧 Processando PedidoPagoEvent [{}] - Pedido: {} - Método: {}",
                event.getEventId(), event.getPedidoId(), event.getMetodoPagamento());

        try {
            // Enviar email de confirmação de pagamento
            emailService.enviarEmailPedidoPago(
                    event.getEmailUsuario(),
                    event.getPedidoId(),
                    event.getValorPago(),
                    event.getMetodoPagamento()
            );

            // Criar notificação in-app
            notificationService.criarNotificacao(
                    event.getUsuarioId(),
                    "💰 Pagamento Confirmado",
                    String.format("Pagamento do pedido #%s confirmado via %s! Valor: R$ %.2f",
                            event.getPedidoId().toString().substring(0, 8),
                            event.getMetodoPagamento(),
                            event.getValorPago())
            );

            log.info("✅ PedidoPagoEvent processado com sucesso: {} - {}",
                    event.getPedidoId(), event.getMetodoPagamento());

        } catch (Exception e) {
            log.error("❌ Erro ao processar PedidoPagoEvent [{}] - Pedido: {}",
                    event.getEventId(), event.getPedidoId(), e);
            // TODO: Enviar para Dead Letter Queue para retry
        }
    }

    // ========================================================================
    // PEDIDO ENVIADO
    // ========================================================================

    /**
     * Quando pedido é enviado → Enviar email com código de rastreio
     *
     * Event disparado em: Pedido.enviar(codigoRastreio, transportadora, telefone)
     * Campos disponíveis: pedidoId, usuarioId, nomeUsuario, emailUsuario, telefone, codigoRastreio, transportadora
     *
     * NOTA: O construtor do evento usa ordem: (id, usuarioId, nomeUsuario, emailUsuario, telefone, rastreio, transportadora)
     */
    @Async
    @EventListener
    public void handlePedidoEnviado(PedidoEnviadoEvent event) {
        log.info("🎧 Processando PedidoEnviadoEvent [{}] - Pedido: {} - Rastreio: {}",
                event.getEventId(), event.getPedidoId(), event.getCodigoRastreio());

        try {
            // Enviar email com código de rastreio
            emailService.enviarEmailPedidoEnviado(
                    event.getEmailUsuario(),
                    event.getNomeUsuario(),
                    event.getPedidoId(),
                    event.getCodigoRastreio(),
                    event.getTransportadora(),
                    event.getTelefone()
            );

            // Criar notificação in-app
            notificationService.criarNotificacao(
                    event.getUsuarioId(),
                    "📦 Pedido Enviado",
                    String.format("Seu pedido #%s foi enviado via %s! Código de rastreio: %s",
                            event.getPedidoId().toString().substring(0, 8),
                            event.getTransportadora(),
                            event.getCodigoRastreio())
            );

            log.info("✅ PedidoEnviadoEvent processado com sucesso: {} - Rastreio: {} - Transportadora: {}",
                    event.getPedidoId(), event.getCodigoRastreio(), event.getTransportadora());

        } catch (Exception e) {
            log.error("❌ Erro ao processar PedidoEnviadoEvent [{}] - Pedido: {}",
                    event.getEventId(), event.getPedidoId(), e);
            // TODO: Enviar para Dead Letter Queue para retry
        }
    }

    // ========================================================================
    // PEDIDO CANCELADO
    // ========================================================================

    /**
     * Quando pedido é cancelado → Enviar email de cancelamento
     *
     * Event disparado em: Pedido.cancelar(motivo)
     * Campos disponíveis: pedidoId, usuarioId, emailUsuario, motivo
     *
     * NOTA: O evento NÃO tem valorTotal (diferente dos outros)
     */
    @Async
    @EventListener
    public void handlePedidoCancelado(PedidoCanceladoEvent event) {
        log.info("🎧 Processando PedidoCanceladoEvent [{}] - Pedido: {} - Motivo: {}",
                event.getEventId(), event.getPedidoId(), event.getMotivo());

        try {
            // Enviar email de cancelamento
            emailService.enviarEmailPedidoCancelado(
                    event.getEmailUsuario(),
                    event.getPedidoId(),
                    event.getMotivo()
            );

            // Criar notificação in-app
            notificationService.criarNotificacao(
                    event.getUsuarioId(),
                    "❌ Pedido Cancelado",
                    String.format("Pedido #%s foi cancelado. Motivo: %s",
                            event.getPedidoId().toString().substring(0, 8),
                            event.getMotivo())
            );

            log.info("✅ PedidoCanceladoEvent processado com sucesso: {} - Motivo: {}",
                    event.getPedidoId(), event.getMotivo());

        } catch (Exception e) {
            log.error("❌ Erro ao processar PedidoCanceladoEvent [{}] - Pedido: {}",
                    event.getEventId(), event.getPedidoId(), e);
            // TODO: Enviar para Dead Letter Queue para retry
        }
    }
}
