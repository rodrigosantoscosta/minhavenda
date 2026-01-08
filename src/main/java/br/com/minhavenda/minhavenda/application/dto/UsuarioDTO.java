package br.com.minhavenda.minhavenda.application.dto;

import br.com.minhavenda.minhavenda.domain.entity.Usuario;
import br.com.minhavenda.minhavenda.domain.enums.TipoUsuario;
import lombok.*;

import javax.management.relation.Role;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO de resposta para Usuario.
 *
 * Usado em:
 * - GET /usuarios
 * - GET /usuarios/{id}
 * - Response após atualizar
 *
 * Não retorna senha por segurança!
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {

    private UUID id;
    private String nome;
    private String email;
//    private String cpf;
//    private String telefone;
    private TipoUsuario tipo;
    private Boolean ativo;
    private Instant dataCadastro;
}