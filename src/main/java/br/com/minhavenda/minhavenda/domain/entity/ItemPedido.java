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

    /**
     * Calcula o subtotal antes de persistir.
     * Este método é chamado automaticamente pelo JPA.
     */
    @PrePersist
    @PreUpdate
    protected void calcularSubtotal() {
        this.subtotal = calcularSubtotalAtual();
    }

    /**
     * Retorna o subtotal do item.
     * Se ainda não foi persistido (subtotal == null), calcula dinamicamente.
     *
     * @return subtotal calculado
     */
    public BigDecimal getSubtotal() {
        if (this.subtotal != null) {
            return this.subtotal;
        }
        return calcularSubtotalAtual();  // Calcula dinamicamente se ainda não foi salvo
    }

    /**
     * Calcula o subtotal atual (quantidade * preço).
     *
     * @return subtotal calculado
     */
    private BigDecimal calcularSubtotalAtual() {
        if (quantidade != null && precoUnitario != null) {
            return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        }
        return BigDecimal.ZERO;
    }
}
