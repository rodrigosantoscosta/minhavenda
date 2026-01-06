package br.com.minhavenda.minhavenda.domain.entity;

import br.com.minhavenda.minhavenda.domain.valueobject.Money;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pagamentos", indexes = {
    @Index(name = "idx_pagamento_pedido", columnList = "pedido_id"),
    @Index(name = "idx_pagamento_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Metodo metodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private Status status = Status.PENDING;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "valor", column = @Column(name = "valor", nullable = false, precision = 10, scale = 2)),
        @AttributeOverride(name = "moeda", column = @Column(name = "moeda_pagamento", length = 3))
    })
    private Money valor;

    @Column(name = "processado_em")
    private Instant processadoEm;

    // Métodos de negócio
    public void aprovar() {
        if (this.status == Status.APPROVED) {
            throw new IllegalStateException("Pagamento já foi aprovado");
        }
        if (this.status == Status.FAILED) {
            throw new IllegalStateException("Pagamento falhou, não pode ser aprovado");
        }
        this.status = Status.APPROVED;
        this.processadoEm = Instant.now();
    }

    public void falhar(String motivo) {
        if (this.status == Status.APPROVED) {
            throw new IllegalStateException("Pagamento aprovado não pode falhar");
        }
        this.status = Status.FAILED;
        this.processadoEm = Instant.now();
        // Aqui você poderia adicionar um campo 'motivoFalha' se necessário
    }

    public void reprocessar() {
        if (this.status != Status.FAILED) {
            throw new IllegalStateException("Apenas pagamentos com falha podem ser reprocessados");
        }
        this.status = Status.PENDING;
        this.processadoEm = null;
    }

    public boolean isAprovado() {
        return this.status == Status.APPROVED;
    }

    public boolean isPendente() {
        return this.status == Status.PENDING;
    }

    public boolean isFalhou() {
        return this.status == Status.FAILED;
    }

    public boolean isProcessado() {
        return this.processadoEm != null;
    }

    public boolean isValorIgualA(Money outroValor) {
        return this.valor.getValor().equals(outroValor.getValor());
    }

    public enum Metodo {
        CARTAO("Cartão de Crédito"),
        PIX("PIX"),
        BOLETO("Boleto Bancário");

        private final String descricao;

        Metodo(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum Status {
        PENDING("Pendente"),
        APPROVED("Aprovado"),
        FAILED("Falhou");

        private final String descricao;

        Status(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }
}
