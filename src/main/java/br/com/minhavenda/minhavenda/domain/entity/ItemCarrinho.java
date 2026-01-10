package br.com.minhavenda.minhavenda.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidade Item do Carrinho.
 *
 * Representa um produto dentro do carrinho.
 *
 * Relacionamentos:
 * - N:1 com Carrinho (muitos itens pertencem a um carrinho)
 * - N:1 com Produto (muitos itens referenciam um produto)
 *
 * IMPORTANTE:
 * - preco_unitario é um "snapshot" do preço no momento da adição
 * - Mesmo se o produto mudar de preço, o carrinho mantém o preço original
 * - subtotal é calculado automaticamente (quantidade × preco_unitario)
 */
@Entity
@Table(
        name = "itens_carrinho",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_carrinho_produto",
                columnNames = {"carrinho_id", "produto_id"}
        )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCarrinho {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Carrinho ao qual este item pertence.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrinho_id", nullable = false)
    private Carrinho carrinho;

    /**
     * Produto referenciado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    /**
     * Quantidade do produto.
     * Deve ser sempre maior que zero.
     */
    @Column(nullable = false)
    private Integer quantidade;

    /**
     * Preço unitário do produto no momento da adição ao carrinho.
     *
     * Este é um "snapshot" do preço.
     * Mesmo se o produto mudar de preço depois, o carrinho
     * mantém o preço que estava quando foi adicionado.
     */
    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    /**
     * Subtotal do item (quantidade × preco_unitario).
     * Calculado automaticamente.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    /**
     * Calcula o subtotal do item.
     *
     * subtotal = quantidade × preco_unitario
     *
     * Deve ser chamado sempre que quantidade ou preço mudar.
     */
    public void calcularSubtotal() {
        if (quantidade != null && precoUnitario != null) {
            this.subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    /**
     * Incrementa a quantidade do item.
     *
     * @param quantidade quantidade a adicionar
     */
    public void incrementarQuantidade(Integer quantidade) {
        this.quantidade += quantidade;
        calcularSubtotal();
    }

    /**
     * Define a quantidade e recalcula o subtotal.
     *
     * @param quantidade nova quantidade
     */
    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
        calcularSubtotal();
    }

    /**
     * Lifecycle callback executado antes de persistir.
     * Garante que subtotal está calculado.
     */
    @PrePersist
    @PreUpdate
    private void calcularSubtotalAutomaticamente() {
        calcularSubtotal();
    }
}