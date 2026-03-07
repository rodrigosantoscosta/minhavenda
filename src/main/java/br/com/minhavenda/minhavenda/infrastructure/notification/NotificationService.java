package br.com.minhavenda.minhavenda.infrastructure.notification;



import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Slf4j
@Service
public class NotificationService {

    /**
     * Criar notificação in-app
     * TODO: Salvar no banco + enviar via WebSocket se usuário online
     */
    public void criarNotificacao(UUID usuarioId, String titulo, String mensagem) {
        log.info("🔔 Notificação para {}: {} - {}", usuarioId, titulo, mensagem);

        // TODO: Salvar no banco de dados
        // Notificacao notificacao = new Notificacao(usuarioId, titulo, mensagem);
        // notificacaoRepository.save(notificacao);

        // TODO: Enviar via WebSocket se usuário estiver online
        // messagingTemplate.convertAndSendToUser(
        //     usuarioId.toString(),
        //     "/queue/notifications",
        //     notificacao
        // );

        log.info("✅ Notificação criada");
    }

    /**
     * Push notification (para mobile app)
     * TODO: Integrar com Firebase Cloud Messaging
     */
    public void enviarPushNotification(UUID usuarioId, String titulo, String corpo) {
        log.info("📲 Push notification para {}: {}", usuarioId, titulo);

        // TODO: Integrar com FCM
        // Message message = Message.builder()
        //     .setNotification(Notification.builder()
        //         .setTitle(titulo)
        //         .setBody(corpo)
        //         .build())
        //     .setToken(userDeviceToken)
        //     .build();
        //
        // firebaseMessaging.send(message);

        log.info("✅ Push notification enviado (simulado)");
    }
}
