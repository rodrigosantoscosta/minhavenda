package br.com.minhavenda.minhavenda.domain.entity;

import br.com.minhavenda.minhavenda.domain.valueobject.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notificacoes", indexes = {
    @Index(name = "idx_notificacao_usuario", columnList = "usuario_id"),
    @Index(name = "idx_notificacao_enviado", columnList = "enviado")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Tipo tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enviado = false;

    @Column(name = "enviado_em")
    private Instant enviadoEm;

    // Factory methods
    public static Notificacao criarEmail(Usuario usuario, String mensagem) {
        validarMensagem(mensagem);
        return Notificacao.builder()
            .usuario(usuario)
            .tipo(Tipo.EMAIL)
            .mensagem(mensagem)
            .build();
    }

    public static Notificacao criarSms(Usuario usuario, String mensagem) {
        validarMensagem(mensagem);
        if (mensagem.length() > 160) {
            throw new IllegalArgumentException("Mensagem SMS não pode exceder 160 caracteres");
        }
        return Notificacao.builder()
            .usuario(usuario)
            .tipo(Tipo.SMS)
            .mensagem(mensagem)
            .build();
    }

    // Métodos de negócio
    public void marcarComoEnviada() {
        if (this.enviado) {
            throw new IllegalStateException("Notificação já foi enviada");
        }
        this.enviado = true;
        this.enviadoEm = Instant.now();
    }

    public void reenviar() {
        if (!this.enviado) {
            throw new IllegalStateException("Notificação ainda não foi enviada pela primeira vez");
        }
        this.enviado = false;
        this.enviadoEm = null;
    }

    public boolean isPendente() {
        return !this.enviado;
    }

    public boolean isEmail() {
        return this.tipo == Tipo.EMAIL;
    }

    public boolean isSms() {
        return this.tipo == Tipo.SMS;
    }

    private static void validarMensagem(String mensagem) {
        if (mensagem == null || mensagem.trim().isEmpty()) {
            throw new IllegalArgumentException("Mensagem não pode ser vazia");
        }
    }

    public enum Tipo {
        EMAIL("E-mail"),
        SMS("SMS");

        private final String descricao;

        Tipo(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }
}
