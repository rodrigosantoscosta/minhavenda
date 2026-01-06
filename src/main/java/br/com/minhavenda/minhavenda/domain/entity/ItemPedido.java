package br.com.minhavenda.minhavenda.domain.entity;


import br.com.minhavenda.minhavenda.domain.valueobject.Money;
import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "itens_pedido", indexes = {
    @Index(name = "idx_item_pedido", columnList = "pedido_id"),
    @Index(name = "idx_item_produto", columnList = "produto_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    @Setter // Setter apenas para Pedido poder adicionar o item
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private Integer quantidade;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "valor", column = @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)),
        @AttributeOverride(name = "moeda", column = @Column(name = "moeda_preco", length = 3))
    })
    private Money precoUnitario;

    // Métodos de negócio
    public void atualizarQuantidade(Integer novaQuantidade) {
        if (novaQuantidade == null || novaQuantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        this.quantidade = novaQuantidade;
    }

    public Money calcularSubtotal() {
        return precoUnitario.multiplicar(quantidade);
    }

    public boolean isProduto(Produto produto) {
        return this.produto.getId().equals(produto.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemPedido)) return false;
        ItemPedido that = (ItemPedido) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
