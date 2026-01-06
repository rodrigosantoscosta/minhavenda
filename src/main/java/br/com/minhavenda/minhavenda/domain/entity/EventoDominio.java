package br.com.minhavenda.minhavenda.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "eventos_dominio", indexes = {
    @Index(name = "idx_evento_tipo", columnList = "tipo"),
    @Index(name = "idx_evento_data", columnList = "data_publicacao")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EventoDominio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String tipo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "json")
    @Builder.Default
    private Map<String, Object> payload = new HashMap<>();

    @CreationTimestamp
    @Column(name = "data_publicacao", nullable = false, updatable = false)
    private Instant dataPublicacao;

    // Factory methods
    public static EventoDominio criar(String tipo, Map<String, Object> payload) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo do evento não pode ser vazio");
        }
        if (payload == null) {
            payload = new HashMap<>();
        }

        return EventoDominio.builder()
            .tipo(tipo.trim())
            .payload(new HashMap<>(payload)) // Cópia defensiva
            .build();
    }

    // Métodos auxiliares para eventos específicos
    public static EventoDominio pedidoCriado(UUID pedidoId, UUID usuarioId, Map<String, Object> dados) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("pedidoId", pedidoId.toString());
        payload.put("usuarioId", usuarioId.toString());
        if (dados != null) {
            payload.putAll(dados);
        }
        return criar("PEDIDO_CRIADO", payload);
    }

    public static EventoDominio pedidoPago(UUID pedidoId, UUID pagamentoId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("pedidoId", pedidoId.toString());
        payload.put("pagamentoId", pagamentoId.toString());
        return criar("PEDIDO_PAGO", payload);
    }

    public static EventoDominio pedidoCancelado(UUID pedidoId, String motivo) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("pedidoId", pedidoId.toString());
        payload.put("motivo", motivo);
        return criar("PEDIDO_CANCELADO", payload);
    }

    public static EventoDominio pedidoEnviado(UUID pedidoId, UUID entregaId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("pedidoId", pedidoId.toString());
        payload.put("entregaId", entregaId.toString());
        return criar("PEDIDO_ENVIADO", payload);
    }

    public static EventoDominio estoqueAtualizado(UUID produtoId, Integer quantidadeAnterior, Integer quantidadeAtual) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("produtoId", produtoId.toString());
        payload.put("quantidadeAnterior", quantidadeAnterior);
        payload.put("quantidadeAtual", quantidadeAtual);
        return criar("ESTOQUE_ATUALIZADO", payload);
    }

    // Métodos de consulta
    public Object getPayloadValue(String key) {
        return payload.get(key);
    }

    public String getPayloadAsString(String key) {
        Object value = payload.get(key);
        return value != null ? value.toString() : null;
    }

    public boolean isTipo(String tipoEsperado) {
        return this.tipo.equals(tipoEsperado);
    }

    public Map<String, Object> getPayload() {
        return new HashMap<>(payload); // Cópia defensiva
    }
}
