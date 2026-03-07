package br.com.minhavenda.minhavenda.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de mensagem para o evento PedidoCancelado publicado no RabbitMQ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoCanceladoMessage {

    private UUID eventId;
    private UUID pedidoId;
    private UUID usuarioId;
    private String emailUsuario;
    private String motivo;
    private Instant ocorridoEm;
}
