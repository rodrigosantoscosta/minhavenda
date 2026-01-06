package br.com.minhavenda.minhavenda.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "categorias", indexes = {
    @Index(name = "idx_categoria_nome", columnList = "nome"),
    @Index(name = "idx_categoria_ativo", columnList = "ativo")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(nullable = true, unique = true, length = 100)
    private String descricao;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;



    @CreationTimestamp
    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private Instant dataCadastro;

    // Métodos de negócio
    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    public void renomear(String novoNome) {
        if (novoNome == null || novoNome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da categoria não pode ser vazio");
        }
        this.nome = novoNome.trim();
    }
}
