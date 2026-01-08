package br.com.minhavenda.minhavenda.domain.entity;

import br.com.minhavenda.minhavenda.domain.enums.TipoUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuarios", indexes = {
    @Index(name = "idx_usuario_email", columnList = "email")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    private TipoUsuario tipo;

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

    public boolean isAtivo() {
        return this.ativo != null && this.ativo;
    }

    public boolean isAdmin() {
        return this.tipo == TipoUsuario.ADMIN;
    }

    public boolean isCliente() {
        return this.tipo == TipoUsuario.CLIENTE;
    }

    public void atualizarSenha(String novaSenhaHash) {
        if (novaSenhaHash == null || novaSenhaHash.isBlank()) {
            throw new IllegalArgumentException("Senha hash não pode ser vazia");
        }
        this.senhaHash = novaSenhaHash;
    }
}
