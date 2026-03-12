package br.com.minhavenda.minhavenda.infrastructure.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro centralizado de SseEmitters por usuario.
 *
 * Cada usuario autenticado pode ter um unico emitter ativo.
 * Quando o RabbitMQ consumer processa um evento de pedido,
 * ele notifica o usuario correspondente via SSE.
 *
 * Thread-safe: usa ConcurrentHashMap.
 */
@Slf4j
@Component
public class SseEmitterRegistry {

    private static final long SSE_TIMEOUT_MS = 5 * 60 * 1000L; // 5 minutos

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Registra um novo emitter para o usuario e remove o anterior, se houver.
     *
     * @param userId ID do usuario autenticado
     * @return o novo SseEmitter configurado
     */
    public SseEmitter register(UUID userId) {
        // Remove emitter anterior se existir
        SseEmitter existing = emitters.remove(userId);
        if (existing != null) {
            existing.complete();
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.debug("[SSE] Emitter concluido para usuario: {}", userId);
        });

        emitter.onTimeout(() -> {
            emitters.remove(userId);
            log.debug("[SSE] Emitter timeout para usuario: {}", userId);
        });

        emitter.onError(ex -> {
            emitters.remove(userId);
            log.debug("[SSE] Emitter erro para usuario: {} — {}", userId, ex.getMessage());
        });

        emitters.put(userId, emitter);
        log.info("[SSE] Emitter registrado para usuario: {} | total ativos: {}", userId, emitters.size());
        return emitter;
    }

    /**
     * Envia um evento SSE para o usuario especificado.
     * Se o usuario nao estiver conectado, o evento e descartado silenciosamente.
     *
     * @param userId    ID do usuario destinatario
     * @param eventName nome do evento (ex: "pedido.pago")
     * @param data      payload do evento (objeto sera serializado como JSON pelo Spring)
     */
    public void sendEvent(UUID userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            log.debug("[SSE] Nenhum emitter ativo para usuario: {} (evento '{}' descartado)", userId, eventName);
            return;
        }

        try {
            emitter.send(
                    SseEmitter.event()
                            .name(eventName)
                            .data(data)
            );
            log.info("[SSE] Evento '{}' enviado para usuario: {}", eventName, userId);
        } catch (IOException e) {
            log.warn("[SSE] Falha ao enviar evento '{}' para usuario: {} — removendo emitter", eventName, userId);
            emitters.remove(userId);
        }
    }

    /**
     * Remove e fecha o emitter do usuario.
     *
     * @param userId ID do usuario
     */
    public void remove(UUID userId) {
        SseEmitter emitter = emitters.remove(userId);
        if (emitter != null) {
            emitter.complete();
        }
    }

    /**
     * Retorna o numero de conexoes SSE ativas.
     */
    public int activeConnections() {
        return emitters.size();
    }
}
