package br.com.minhavenda.minhavenda.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de mensagem para o evento PedidoPago publicado no RabbitMQ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoPagoMessage {

    private UUID eventId;
    private UUID pedidoId;
    private UUID usuarioId;
    private String emailUsuario;
    private Double valorPago;
    private String metodoPagamento;
    private Instant ocorridoEm;
}
