package br.com.minhavenda.minhavenda.application.mapper;

import br.com.minhavenda.minhavenda.application.dto.UsuarioDTO;
import br.com.minhavenda.minhavenda.domain.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {
    public UsuarioDTO toDTO(Usuario usuario){
        if(usuario == null){
            return null;
        }

        return UsuarioDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
//                .cpf(usuario.getCpf())
//                .telefone(usuario.getTelefone())
                .tipo(usuario.getTipo())
                .ativo(usuario.getAtivo())
                .dataCadastro(usuario.getDataCadastro())
                .build();
    }
}
