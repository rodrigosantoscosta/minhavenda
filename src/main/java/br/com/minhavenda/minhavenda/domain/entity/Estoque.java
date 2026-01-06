package br.com.minhavenda.minhavenda.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "estoques", indexes = {
    @Index(name = "idx_estoque_produto", columnList = "produto_id", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false, unique = true)
    private Produto produto;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantidade = 0;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = Instant.now();
    }

    // Métodos de negócio
    public void adicionar(Integer quantidadeAdicionar) {
        if (quantidadeAdicionar == null || quantidadeAdicionar <= 0) {
            throw new IllegalArgumentException("Quantidade a adicionar deve ser maior que zero");
        }
        this.quantidade += quantidadeAdicionar;
    }

    public void remover(Integer quantidadeRemover) {
        if (quantidadeRemover == null || quantidadeRemover <= 0) {
            throw new IllegalArgumentException("Quantidade a remover deve ser maior que zero");
        }
        if (quantidadeRemover > this.quantidade) {
            throw new IllegalStateException(
                String.format("Estoque insuficiente. Disponível: %d, Solicitado: %d", 
                    this.quantidade, quantidadeRemover)
            );
        }
        this.quantidade -= quantidadeRemover;
    }

    public void reservar(Integer quantidadeReservar) {
        remover(quantidadeReservar); // Usa a mesma lógica de remoção
    }

    public void liberar(Integer quantidadeLiberar) {
        adicionar(quantidadeLiberar); // Usa a mesma lógica de adição
    }

    public void ajustar(Integer novaQuantidade) {
        if (novaQuantidade == null || novaQuantidade < 0) {
            throw new IllegalArgumentException("Quantidade não pode ser negativa");
        }
        this.quantidade = novaQuantidade;
    }

    public boolean temEstoqueSuficiente(Integer quantidadeNecessaria) {
        return this.quantidade >= quantidadeNecessaria;
    }

    public boolean isEstoqueBaixo(Integer limiteMinimo) {
        return this.quantidade <= limiteMinimo;
    }

    public boolean isSemEstoque() {
        return this.quantidade == 0;
    }
}
