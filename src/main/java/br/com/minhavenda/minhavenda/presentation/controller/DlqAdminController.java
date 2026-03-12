package br.com.minhavenda.minhavenda.presentation.controller;

import br.com.minhavenda.minhavenda.infrastructure.config.RabbitMQConfig;
import br.com.minhavenda.minhavenda.infrastructure.messaging.DlqRequeueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller administrativo para gerenciamento das Dead Letter Queues do RabbitMQ.
 *
 * Permite reprocessar mensagens que falharam no consumer e foram enviadas
 * para as DLQs (*.dlq), republicando-as no exchange principal para nova tentativa.
 *
 * Endpoints:
 *   POST /api/admin/dlq/requeue/{queue}  — requeue de uma DLQ especifica
 *   POST /api/admin/dlq/requeue-all      — requeue de todas as DLQs
 *   GET  /api/admin/dlq/queues           — lista as DLQs disponiveis
 */
@Slf4j
@RestController
@RequestMapping("/admin/dlq")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - DLQ", description = "Gerenciamento de Dead Letter Queues do RabbitMQ")
@SecurityRequirement(name = "bearer-auth")
public class DlqAdminController {

    private final DlqRequeueService dlqRequeueService;

    private static final List<String> DLQ_NAMES = List.of(
            RabbitMQConfig.DLQ_PEDIDO_CRIADO,
            RabbitMQConfig.DLQ_PEDIDO_PAGO,
            RabbitMQConfig.DLQ_PEDIDO_ENVIADO,
            RabbitMQConfig.DLQ_PEDIDO_CANCELADO
    );

    /**
     * Lista as DLQs disponiveis para requeue.
     *
     * GET /api/admin/dlq/queues
     */
    @GetMapping("/queues")
    @Operation(
            summary = "Listar DLQs disponiveis",
            description = "Retorna os nomes de todas as Dead Letter Queues do sistema"
    )
    public ResponseEntity<Map<String, Object>> listarDlqs() {
        return ResponseEntity.ok(Map.of(
                "dlqs", DLQ_NAMES,
                "dica", "Use POST /admin/dlq/requeue/{queue} para reprocessar mensagens de uma fila especifica",
                "managementUI", "http://localhost:15672"
        ));
    }

    /**
     * Reprocessa todas as mensagens de uma DLQ especifica.
     *
     * POST /api/admin/dlq/requeue/{queue}
     *
     * Exemplos de {queue}:
     *   pedidos.criado.dlq
     *   pedidos.pago.dlq
     *   pedidos.enviado.dlq
     *   pedidos.cancelado.dlq
     *
     * @param queue nome da DLQ
     * @return resultado com quantidade de mensagens reenfileiradas
     */
    @PostMapping("/requeue/{queue}")
    @Operation(
            summary = "Requeue de uma DLQ",
            description = "Reprocessa todas as mensagens de uma Dead Letter Queue especifica, " +
                          "republicando-as no exchange principal para nova tentativa de processamento"
    )
    @ApiResponse(responseCode = "200", description = "Requeue realizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Nome de DLQ invalido")
    public ResponseEntity<Map<String, Object>> requeue(
            @Parameter(
                    description = "Nome da DLQ",
                    example = "pedidos.criado.dlq"
            )
            @PathVariable String queue
    ) {
        log.info("[DLQ Admin] Requeue solicitado para: {}", queue);
        try {
            int count = dlqRequeueService.requeue(queue);
            return ResponseEntity.ok(Map.of(
                    "dlq", queue,
                    "mensagensReenfileiradas", count,
                    "status", count > 0 ? "OK — mensagens reenviadas para reprocessamento" : "DLQ estava vazia"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "erro", e.getMessage(),
                    "dlqsValidas", DLQ_NAMES
            ));
        }
    }

    /**
     * Reprocessa todas as mensagens de todas as DLQs.
     *
     * POST /api/admin/dlq/requeue-all
     *
     * @return mapa com resultado por DLQ
     */
    @PostMapping("/requeue-all")
    @Operation(
            summary = "Requeue de todas as DLQs",
            description = "Reprocessa mensagens de todas as Dead Letter Queues de uma so vez"
    )
    @ApiResponse(responseCode = "200", description = "Requeue geral realizado")
    public ResponseEntity<Map<String, Object>> requeueAll() {
        log.info("[DLQ Admin] Requeue-all solicitado");
        Map<String, Integer> resultado = dlqRequeueService.requeueAll();
        int total = resultado.values().stream().mapToInt(Integer::intValue).sum();
        return ResponseEntity.ok(Map.of(
                "resultadoPorDlq", resultado,
                "totalMensagensReenfileiradas", total,
                "status", total > 0 ? "OK" : "Todas as DLQs estavam vazias"
        ));
    }
}
