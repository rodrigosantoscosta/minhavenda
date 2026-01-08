package br.com.minhavenda.minhavenda.domain.entity;

import br.com.minhavenda.minhavenda.domain.valueobject.Money;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
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

    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nome;

    @Size(max = 1000, message = "A descrição não pode ter mais de 1000 caracteres")
    @Column(columnDefinition = "TEXT")
    private String descricao;
    
    @Size(max = 255, message = "A URL da imagem é muito longa")
    @URL(message = "URL da imagem inválida")
    @Column(name = "url_imagem")
    private String urlImagem;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço deve ser maior que zero")
    @Digits(integer = 10, fraction = 2, message = "O preço deve ter no máximo 10 dígitos, sendo 2 decimais")
    @Column(name = "peso_kg", precision = 10, scale = 2)
    private BigDecimal pesoKg;
    
    @OneToOne(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    private Estoque estoque;
    
    @Min(value = 0, message = "A altura não pode ser negativa")
    @Max(value = 1000, message = "A altura máxima permitida é 1000cm")
    @Column(name = "altura_cm")
    private Integer alturaCm;
    
    @Min(value = 0, message = "A largura não pode ser negativa")
    @Max(value = 1000, message = "A largura máxima permitida é 1000cm")
    @Column(name = "largura_cm")
    private Integer larguraCm;
    
    @Min(value = 0, message = "O comprimento não pode ser negativo")
    @Max(value = 1000, message = "O comprimento máximo permitido é 1000cm")
    @Column(name = "comprimento_cm")
    private Integer comprimentoCm;

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
        if (novoPreco.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O preço deve ser maior que zero");
        }
        this.preco = novoPreco;
    }

    public void atualizarCategoria(Categoria novaCategoria) {
        this.categoria = novaCategoria;
    }

    public void atualizarInformacoes(String nome, String descricao, String urlImagem) {
        setNome(nome);
        this.descricao = descricao != null ? descricao.trim() : null;
        this.urlImagem = urlImagem;
    }
    
    public void adicionarEstoque(Integer quantidade) {
        if (this.estoque == null) {
            this.estoque = Estoque.builder()
                .produto(this)
                .quantidade(0)
                .build();
        }
        this.estoque.adicionar(quantidade);
    }
    
    public void removerEstoque(Integer quantidade) {
        if (this.estoque == null) {
            throw new IllegalStateException("Produto não possui registro de estoque");
        }
        this.estoque.remover(quantidade);
    }
    
    public boolean temEstoqueSuficiente(Integer quantidadeDesejada) {
        if (this.estoque == null) {
            return false;
        }
        return this.estoque.getQuantidade() >= quantidadeDesejada;
    }
    
    private void setNome(String nome) {
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
