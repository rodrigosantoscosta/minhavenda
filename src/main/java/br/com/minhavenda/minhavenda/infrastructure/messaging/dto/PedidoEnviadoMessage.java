package br.com.minhavenda.minhavenda.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de mensagem para o evento PedidoEnviado publicado no RabbitMQ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoEnviadoMessage {

    private UUID eventId;
    private UUID pedidoId;
    private UUID usuarioId;
    private String nomeUsuario;
    private String emailUsuario;
    private String telefone;
    private String codigoRastreio;
    private String transportadora;
    private Instant ocorridoEm;
}
