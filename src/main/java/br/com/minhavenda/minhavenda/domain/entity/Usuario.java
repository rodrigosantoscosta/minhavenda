package br.com.minhavenda.minhavenda.domain.valueobject;

import br.com.minhavenda.minhavenda.domain.valueobject.Email;
import jakarta.persistence.*;
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

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "email", nullable = false, unique = true))
    private Email email;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

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

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public boolean isCustomer() {
        return this.role == Role.CUSTOMER;
    }

    public void atualizarSenha(String novaSenhaHash) {
        if (novaSenhaHash == null || novaSenhaHash.isBlank()) {
            throw new IllegalArgumentException("Senha hash não pode ser vazia");
        }
        this.senhaHash = novaSenhaHash;
    }

    public enum Role {
        ADMIN,
        CUSTOMER
    }
}
