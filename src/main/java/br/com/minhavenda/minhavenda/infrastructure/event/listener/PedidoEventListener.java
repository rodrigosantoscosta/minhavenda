package br.com.minhavenda.minhavenda.infrastructure.event.listener;

import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoCriadoEvent;
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
     */
    @Async
    @EventListener
    public void handlePedidoCriado(PedidoCriadoEvent event) {
        log.info("🎧 Processando PedidoCriadoEvent [{}]", event.getEventId());

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

            log.info("✅ PedidoCriadoEvent processado: {}", event.getPedidoId());

        } catch (Exception e) {
            log.error("Erro ao processar PedidoCriadoEvent [{}]",
                    event.getEventId(), e);
            // TODO: Enviar para Dead Letter Queue para retry
        }
    }
}
