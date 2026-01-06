package br.com.minhavenda.minhavenda.domain.entity;

import br.com.minhavenda.minhavenda.domain.valueobject.Money;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "produtos", indexes = {
    @Index(name = "idx_produto_categoria", columnList = "categoria_id"),
    @Index(name = "idx_produto_ativo", columnList = "ativo")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "valor", column = @Column(name = "preco", nullable = false, precision = 10, scale = 2)),
        @AttributeOverride(name = "moeda", column = @Column(name = "moeda", length = 3))
    })
    private Money preco;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private Instant dataCadastro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // Métodos de negócio
    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    public void atualizarPreco(Money novoPreco) {
        if (novoPreco == null) {
            throw new IllegalArgumentException("Preço não pode ser nulo");
        }
        this.preco = novoPreco;
    }

    public void atualizarCategoria(Categoria novaCategoria) {
        this.categoria = novaCategoria;
    }

    public void atualizarInformacoes(String nome, String descricao) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do produto não pode ser vazio");
        }
        this.nome = nome.trim();
        this.descricao = descricao != null ? descricao.trim() : null;
    }

    public boolean isPrecoDiferenteDe(Money outroPreco) {

        return !this.preco.getValor().equals(outroPreco.getValor());
    }
}
