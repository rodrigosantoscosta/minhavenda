package br.com.minhavenda.minhavenda.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "entregas", indexes = {
    @Index(name = "idx_entrega_pedido", columnList = "pedido_id"),
    @Index(name = "idx_entrega_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private Status status = Status.CREATED;

    @Column(name = "endereco_entrega", nullable = false, columnDefinition = "TEXT")
    private String enderecoEntrega;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = Instant.now();
    }

    // Factory method
    public static Entrega criar(Pedido pedido, String enderecoEntrega) {
        if (!pedido.isPago()) {
            throw new IllegalStateException("Apenas pedidos pagos podem ter entrega criada");
        }
        if (enderecoEntrega == null || enderecoEntrega.trim().isEmpty()) {
            throw new IllegalArgumentException("Endereço de entrega é obrigatório");
        }

        return Entrega.builder()
            .pedido(pedido)
            .enderecoEntrega(enderecoEntrega.trim())
            .status(Status.CREATED)
            .build();
    }

    // Métodos de negócio
    public void enviar() {
        if (this.status == Status.SHIPPED) {
            throw new IllegalStateException("Entrega já foi enviada");
        }
        if (this.status == Status.DELIVERED) {
            throw new IllegalStateException("Entrega já foi concluída");
        }
        this.status = Status.SHIPPED;
    }

    public void concluir() {
        if (this.status == Status.DELIVERED) {
            throw new IllegalStateException("Entrega já foi concluída");
        }
        if (this.status != Status.SHIPPED) {
            throw new IllegalStateException("Apenas entregas enviadas podem ser concluídas");
        }
        this.status = Status.DELIVERED;
    }

    public void atualizarEndereco(String novoEndereco) {
        if (this.status != Status.CREATED) {
            throw new IllegalStateException("Endereço só pode ser atualizado antes do envio");
        }
        if (novoEndereco == null || novoEndereco.trim().isEmpty()) {
            throw new IllegalArgumentException("Endereço não pode ser vazio");
        }
        this.enderecoEntrega = novoEndereco.trim();
    }

    public boolean isEnviada() {
        return this.status == Status.SHIPPED;
    }

    public boolean isConcluida() {
        return this.status == Status.DELIVERED;
    }

    public boolean podeSerEnviada() {
        return this.status == Status.CREATED;
    }

    public enum Status {
        CREATED("Criada"),
        SHIPPED("Enviada"),
        DELIVERED("Entregue");

        private final String descricao;

        Status(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }
}
