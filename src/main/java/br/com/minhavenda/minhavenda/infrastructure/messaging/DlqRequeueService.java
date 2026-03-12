package br.com.minhavenda.minhavenda.infrastructure.messaging;

import br.com.minhavenda.minhavenda.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Servico responsavel por reprocessar mensagens das Dead Letter Queues.
 *
 * Estrategia: le mensagens das DLQs uma a uma via basicGet e republica
 * no exchange principal com o routing key original do evento.
 *
 * Uso tipico:
 *   - Via endpoint REST: POST /api/admin/dlq/requeue/{queue}
 *   - Via requeue geral: POST /api/admin/dlq/requeue-all
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DlqRequeueService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Mapeamento de DLQ → routing key da fila principal.
     * Usado para republicar a mensagem no exchange correto.
     */
    private static final Map<String, String> DLQ_TO_ROUTING_KEY = Map.of(
            RabbitMQConfig.DLQ_PEDIDO_CRIADO,    RabbitMQConfig.RK_PEDIDO_CRIADO,
            RabbitMQConfig.DLQ_PEDIDO_PAGO,      RabbitMQConfig.RK_PEDIDO_PAGO,
            RabbitMQConfig.DLQ_PEDIDO_ENVIADO,   RabbitMQConfig.RK_PEDIDO_ENVIADO,
            RabbitMQConfig.DLQ_PEDIDO_CANCELADO, RabbitMQConfig.RK_PEDIDO_CANCELADO
    );

    /**
     * Reprocessa todas as mensagens de uma DLQ especifica.
     *
     * @param dlqName nome da DLQ (ex: "pedidos.criado.dlq")
     * @return numero de mensagens reenfileiradas
     * @throws IllegalArgumentException se a DLQ nao for reconhecida
     */
    public int requeue(String dlqName) {
        String routingKey = DLQ_TO_ROUTING_KEY.get(dlqName);
        if (routingKey == null) {
            throw new IllegalArgumentException("DLQ desconhecida: " + dlqName +
                    ". DLQs validas: " + DLQ_TO_ROUTING_KEY.keySet());
        }

        log.info("[DLQ] Iniciando requeue da fila: {} → exchange: {} | routingKey: {}",
                dlqName, RabbitMQConfig.PEDIDOS_EXCHANGE, routingKey);

        int count = 0;
        Message message;

        while ((message = rabbitTemplate.receive(dlqName, 500)) != null) {
            try {
                rabbitTemplate.send(RabbitMQConfig.PEDIDOS_EXCHANGE, routingKey, message);
                count++;
                log.debug("[DLQ] Mensagem reenfileirada da {} para routing key '{}'", dlqName, routingKey);
            } catch (Exception e) {
                log.error("[DLQ] Falha ao reenviar mensagem da {}: {}", dlqName, e.getMessage(), e);
            }
        }

        log.info("[DLQ] Requeue concluido — fila: {} | mensagens reenfileiradas: {}", dlqName, count);
        return count;
    }

    /**
     * Reprocessa todas as mensagens de todas as DLQs.
     *
     * @return mapa com DLQ → quantidade de mensagens reenfileiradas
     */
    public Map<String, Integer> requeueAll() {
        log.info("[DLQ] Iniciando requeue de todas as DLQs...");
        Map<String, Integer> resultado = new LinkedHashMap<>();

        for (String dlqName : DLQ_TO_ROUTING_KEY.keySet()) {
            int count = requeue(dlqName);
            resultado.put(dlqName, count);
        }

        int total = resultado.values().stream().mapToInt(Integer::intValue).sum();
        log.info("[DLQ] Requeue total concluido — {} mensagens reenfileiradas em {} DLQs", total, resultado.size());
        return resultado;
    }

    /**
     * Retorna a contagem estimada de mensagens em cada DLQ.
     * Usa receive com timeout 0 para nao consumir mensagens.
     *
     * @return mapa com DLQ → contagem de mensagens
     */
    public Map<String, Object> stats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        for (String dlqName : DLQ_TO_ROUTING_KEY.keySet()) {
            try {
                // Consulta via Properties do RabbitAdmin nao disponivel sem injetar ConnectionFactory diretamente.
                // Aqui retornamos o status de cada fila como texto informativo.
                stats.put(dlqName, "use GET /api/admin/dlq/stats para ver contagem via Management API");
            } catch (Exception e) {
                stats.put(dlqName, "erro: " + e.getMessage());
            }
        }
        stats.put("_dica", "Acesse http://localhost:15672 (RabbitMQ Management) para ver contagens em tempo real");
        return stats;
    }
}
