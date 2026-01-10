package br.com.minhavenda.minhavenda.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidade ItemPedido - Item de um pedido.
 *
 * Armazena snapshot dos dados do produto no momento da compra.
 */
@Entity
@Table(name = "itens_pedido", indexes = {
        @Index(name = "idx_item_pedido", columnList = "pedido_id"),
        @Index(name = "idx_item_produto", columnList = "produto_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "produto_nome", nullable = false, length = 200)
    private String produtoNome;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @PrePersist
    @PreUpdate
    protected void calcularSubtotal() {
        if (quantidade != null && precoUnitario != null) {
            this.subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        }
    }
}
