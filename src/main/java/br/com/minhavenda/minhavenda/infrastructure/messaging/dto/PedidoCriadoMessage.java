package br.com.minhavenda.minhavenda.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de mensagem para o evento PedidoCriado publicado no RabbitMQ.
 * Serializado como JSON.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoCriadoMessage {

    private UUID eventId;
    private UUID pedidoId;
    private UUID usuarioId;
    private String emailUsuario;
    private String nomeUsuario;
    private Double valorTotal;
    private Integer quantidadeItens;
    private Instant ocorridoEm;
}
