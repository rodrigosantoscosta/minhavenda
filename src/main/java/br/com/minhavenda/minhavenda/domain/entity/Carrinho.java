package br.com.minhavenda.minhavenda.domain.entity;

import br.com.minhavenda.minhavenda.domain.enums.StatusCarrinho;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidade Carrinho de Compras.
 *
 * Representa o carrinho de um usuário.
 * Um usuário pode ter apenas UM carrinho ATIVO por vez.
 *
 * Relacionamentos:
 * - N:1 com Usuario (um carrinho pertence a um usuário)
 * - 1:N com ItemCarrinho (um carrinho tem vários itens)
 *
 * Status:
 * - ATIVO: Em uso, pode ser editado
 * - FINALIZADO: Convertido em pedido
 * - ABANDONADO: Não finalizado há muito tempo
 */
@Entity
@Table(name = "carrinhos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Carrinho {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Usuário dono do carrinho.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Status do carrinho.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCarrinho status;

    /**
     * Itens do carrinho.
     *
     * CascadeType.ALL: Ao salvar/deletar carrinho, faz o mesmo com itens
     * orphanRemoval: Remove itens órfãos (sem carrinho)
     * mappedBy: Campo "carrinho" em ItemCarrinho é o dono da relação
     */
    @OneToMany(
            mappedBy = "carrinho",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<ItemCarrinho> itens = new ArrayList<>();

    /**
     * Valor total do carrinho.
     * Calculado automaticamente pela soma dos subtotais dos itens.
     */
    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal valorTotal = BigDecimal.ZERO;

    /**
     * Data de criação do carrinho.
     */
    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    /**
     * Data da última atualização.
     */
    @UpdateTimestamp
    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    /**
     * Quantidade total de itens no carrinho.
     */

    @Column(name = "quantidade_total", nullable = false)
    @Builder.Default
    private Integer quantidadeTotal = 0;

    /**
     * Calcula o valor total do carrinho.
     *
     * Soma todos os subtotais dos itens.
     * Deve ser chamado após adicionar/remover/atualizar itens.
     */
    public void calcularValorTotal() {
        this.valorTotal = itens.stream()
                .map(ItemCarrinho::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.quantidadeTotal = itens.stream()
                .mapToInt(ItemCarrinho::getQuantidade)
                .sum();
    }
    /**
     * Adiciona item ao carrinho.
     *
     * @param item item a adicionar
     */
    public void adicionarItem(ItemCarrinho item) {
        itens.add(item);
        item.setCarrinho(this);
        calcularValorTotal();
    }

    /**
     * Remove item do carrinho.
     *
     * @param item item a remover
     */
    public void removerItem(ItemCarrinho item) {
        itens.remove(item);
        item.setCarrinho(null);
        calcularValorTotal();
    }

    /**
     * Limpa todos os itens do carrinho.
     */
    public void limpar() {
        itens.clear();
        valorTotal = BigDecimal.ZERO;
    }

    /**
     * Finaliza o carrinho.
     * Altera status para FINALIZADO.
     */
    public void finalizar() {
        this.status = StatusCarrinho.FINALIZADO;
    }
}