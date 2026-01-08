package br.com.minhavenda.minhavenda.application.mapper;

import br.com.minhavenda.minhavenda.application.dto.UsuarioDTO;
import br.com.minhavenda.minhavenda.domain.entity.Usuario;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre Usuario (Entity) e UsuarioDTO.
 *
 * IMPORTANTE: NUNCA mapeia a senha para o DTO (segurança).
 */
@Component
public class UsuarioMapper {

    /**
     * Converte Entity → DTO (sem senha).
     *
     * @param usuario entidade
     * @return DTO sem senha
     */
    public UsuarioDTO toDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return UsuarioDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .tipo(usuario.getTipo())
                .ativo(usuario.getAtivo())
                .dataCadastro(usuario.getDataCadastro())
                .build();
    }

    /**
     * Converte List<Entity> → List<DTO>.
     *
     * @param usuarios lista de entidades
     * @return lista de DTOs
     */
    public List<UsuarioDTO> toDTO(List<Usuario> usuarios) {
        if (usuarios == null) {
            return null;
        }

        return usuarios.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}