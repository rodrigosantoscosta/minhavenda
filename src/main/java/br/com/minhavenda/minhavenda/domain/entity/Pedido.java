package br.com.minhavenda.minhavenda.domain.entity;

import br.com.minhavenda.minhavenda.domain.valueobject.Money;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pedidos", indexes = {
    @Index(name = "idx_pedido_usuario", columnList = "usuario_id"),
    @Index(name = "idx_pedido_status", columnList = "status"),
    @Index(name = "idx_pedido_data_criacao", columnList = "data_criacao")
})
@Getter
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private Status status = Status.CREATED;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "valor", column = @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)),
        @AttributeOverride(name = "moeda", column = @Column(name = "moeda_total", length = 3))
    })
    private Money valorTotal;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private Instant dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao")
    private Instant dataAtualizacao;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemPedido> itens = new ArrayList<>();

    // Métodos de negócio (Aggregate Root)
    public void adicionarItem(Produto produto, Integer quantidade) {
        validarProdutoAtivo(produto);
        validarQuantidade(quantidade);
        validarPedidoEditavel();

        ItemPedido item = ItemPedido.builder()
            .pedido(this)
            .produto(produto)
            .quantidade(quantidade)
            .precoUnitario(produto.getPreco())
            .build();

        this.itens.add(item);
        recalcularValorTotal();
    }

    public void removerItem(ItemPedido item) {
        validarPedidoEditavel();
        this.itens.remove(item);
        recalcularValorTotal();
    }

    public void atualizarQuantidadeItem(ItemPedido item, Integer novaQuantidade) {
        validarPedidoEditavel();
        validarQuantidade(novaQuantidade);
        
        if (!this.itens.contains(item)) {
            throw new IllegalArgumentException("Item não pertence a este pedido");
        }
        
        item.atualizarQuantidade(novaQuantidade);
        recalcularValorTotal();
    }

    public void limparItens() {
        validarPedidoEditavel();
        this.itens.clear();
        this.valorTotal = Money.zero();
    }

    private void recalcularValorTotal() {
        this.valorTotal = this.itens.stream()
            .map(ItemPedido::calcularSubtotal)
            .reduce(Money.zero(), Money::somar);
    }

    public void confirmarPagamento() {
        if (this.status != Status.CREATED) {
            throw new IllegalStateException("Apenas pedidos criados podem ter pagamento confirmado");
        }
        if (this.itens.isEmpty()) {
            throw new IllegalStateException("Pedido sem itens não pode ser pago");
        }
        this.status = Status.PAID;
    }

    public void cancelar() {
        if (this.status == Status.CANCELED) {
            throw new IllegalStateException("Pedido já está cancelado");
        }
        if (this.status == Status.SHIPPED) {
            throw new IllegalStateException("Pedido já enviado não pode ser cancelado");
        }
        this.status = Status.CANCELED;
    }

    public void marcarComoEnviado() {
        if (this.status != Status.PAID) {
            throw new IllegalStateException("Apenas pedidos pagos podem ser enviados");
        }
        this.status = Status.SHIPPED;
    }

    // Validações
    private void validarPedidoEditavel() {
        if (this.status != Status.CREATED) {
            throw new IllegalStateException("Pedido não pode ser editado no status: " + this.status);
        }
    }

    private void validarProdutoAtivo(Produto produto) {
        if (!produto.getAtivo()) {
            throw new IllegalArgumentException("Produto inativo não pode ser adicionado ao pedido");
        }
    }

    private void validarQuantidade(Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
    }

    // Queries
    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public int getQuantidadeTotalItens() {
        return itens.stream()
            .mapToInt(ItemPedido::getQuantidade)
            .sum();
    }

    public boolean temItens() {
        return !itens.isEmpty();
    }

    public boolean isPago() {
        return this.status == Status.PAID;
    }

    public boolean isCancelado() {
        return this.status == Status.CANCELED;
    }

    public boolean isEnviado() {
        return this.status == Status.SHIPPED;
    }

    public boolean podeSerCancelado() {
        return this.status != Status.CANCELED && this.status != Status.SHIPPED;
    }

    public enum Status {
        CREATED,
        PAID,
        CANCELED,
        SHIPPED
    }
}
