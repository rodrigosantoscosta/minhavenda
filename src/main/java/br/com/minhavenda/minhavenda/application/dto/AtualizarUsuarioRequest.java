package br.com.minhavenda.minhavenda.application.dto;


import jakarta.validation.constraints.Email;

import jakarta.validation.constraints.Size;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtualizarUsuarioRequest {

    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @Email(message = "Email inválido")
    private String email;

//    @Size(max = 15, message = "Telefone deve ter no máximo 15 caracteres")
//    private String telefone;
}