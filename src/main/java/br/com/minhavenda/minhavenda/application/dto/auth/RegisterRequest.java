package br.com.minhavenda.minhavenda.application.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request para cadastro de novo usuário.
 * 
 * Validações:
 * - Nome: obrigatório, 3-100 caracteres
 * - Email: obrigatório, formato válido, único
 * - CPF: obrigatório, formato válido, único
 * - Senha: obrigatória, mínimo 6 caracteres
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    /**
     * CPF - valida formato com ou sem máscara.
     * Aceita: 123.456.789-00 ou 12345678900
     */
//    @NotBlank(message = "CPF é obrigatório")
//    @Pattern(
//            regexp = "\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}",
//            message = "CPF inválido. Use formato: 123.456.789-00"
//    )
//    private String cpf;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String senha;

//    @Size(max = 15, message = "Telefone deve ter no máximo 15 caracteres")
//    private String telefone;
}
