package br.com.minhavenda.minhavenda.domain.entity;

import br.com.minhavenda.minhavenda.domain.enums.StatusPedido;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidade Pedido - Representa um pedido finalizado.
 *
 * Aggregate Root que gerencia seus ItemPedido.
 * Criado quando o usuário finaliza o checkout.
 */
@Entity
@Table(name = "pedidos", indexes = {
        @Index(name = "idx_pedido_usuario", columnList = "usuario_id"),
        @Index(name = "idx_pedido_status", columnList = "status"),
        @Index(name = "idx_pedido_data", columnList = "data_criacao")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemPedido> itens = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusPedido status = StatusPedido.CRIADO;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "valor_frete", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal valorFrete = BigDecimal.ZERO;

    @Column(name = "valor_desconto", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "endereco_entrega", nullable = false, length = 500)
    private String enderecoEntrega;

    @Column(name = "observacoes", length = 1000)
    private String observacoes;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    @UpdateTimestamp
    private LocalDateTime dataAtualizacao;

    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    @Column(name = "data_entrega")
    private LocalDateTime dataEntrega;

    @Column(name = "quantidade_itens", nullable = false)
    private Integer quantidadeItens;

    // ========== MÉTODOS DE NEGÓCIO ==========

    public void adicionarItem(ItemPedido item) {
        this.itens.add(item);
        item.setPedido(this);
    }

    public void calcularValorTotal() {
        this.subtotal = itens.stream()
                .map(item -> {
                    BigDecimal itemSubtotal = item.getSubtotal();
                    if (itemSubtotal == null && item.getQuantidade() != null && item.getPrecoUnitario() != null) {
                        itemSubtotal = item.getPrecoUnitario()
                                .multiply(BigDecimal.valueOf(item.getQuantidade()));
                    }
                    return itemSubtotal != null ? itemSubtotal : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.quantidadeItens = itens.stream()
                .mapToInt(ItemPedido::getQuantidade)
                .sum();

        this.valorTotal = subtotal
                .add(valorFrete)
                .subtract(valorDesconto);
    }

    public void marcarComoPago() {
        if (!status.podePagar()) {
            throw new IllegalStateException(
                    String.format("Pedido com status %s não pode ser pago", status)
            );
        }
        this.status = StatusPedido.PAGO;
        this.dataPagamento = LocalDateTime.now();
    }

    public void marcarComoEnviado() {
        if (!status.podeEnviar()) {
            throw new IllegalStateException(
                    String.format("Pedido com status %s não pode ser enviado", status)
            );
        }
        this.status = StatusPedido.ENVIADO;
        this.dataEnvio = LocalDateTime.now();
    }

    public void marcarComoEntregue() {
        if (status != StatusPedido.ENVIADO) {
            throw new IllegalStateException(
                    String.format("Pedido com status %s não pode ser marcado como entregue", status)
            );
        }
        this.status = StatusPedido.ENTREGUE;
        this.dataEntrega = LocalDateTime.now();
    }

    public void cancelar() {
        if (!status.podeCancelar()) {
            throw new IllegalStateException(
                    String.format("Pedido com status %s não pode ser cancelado", status)
            );
        }
        this.status = StatusPedido.CANCELADO;
    }

    public Integer getQuantidadeTotal() {
        return itens.stream()
                .mapToInt(ItemPedido::getQuantidade)
                .sum();
    }
}
