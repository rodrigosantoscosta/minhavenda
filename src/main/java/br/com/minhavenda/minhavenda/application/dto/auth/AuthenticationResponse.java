package br.com.minhavenda.minhavenda.application.dto.auth;

import lombok.*;

/**
 * Response de autenticação (login/register).
 * 
 * Contém:
 * - Token JWT
 * - Tipo do token (sempre "Bearer")
 * - Email do usuário
 * - Nome do usuário
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationResponse {

    /**
     * Token JWT gerado.
     * Cliente deve incluir em todas requests:
     * Authorization: Bearer <token>
     */
    private String token;

    /**
     * Tipo do token (sempre "Bearer").
     */
    @Builder.Default
    private String type = "Bearer";

    /**
     * Email do usuário autenticado.
     */
    private String email;

    /**
     * Nome do usuário autenticado.
     */
    private String nome;
}
