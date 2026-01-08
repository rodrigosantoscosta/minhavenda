package br.com.minhavenda.minhavenda.application.usecase.usuario;

import br.com.minhavenda.minhavenda.application.dto.UsuarioDTO;
import br.com.minhavenda.minhavenda.application.mapper.UsuarioMapper;
import br.com.minhavenda.minhavenda.domain.entity.Usuario;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para buscar perfil do usuário logado.
 *
 * Responsabilidades:
 * - Buscar usuário por email
 * - Converter para DTO (sem senha)
 * - Retornar dados do perfil
 */
@Service
@RequiredArgsConstructor
public class BuscarPerfilUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    /**
     * Busca dados do usuário logado por email.
     *
     * @param email email do usuário autenticado
     * @return DTO com dados do usuário (sem senha)
     * @throws RuntimeException se usuário não encontrado
     */
    @Transactional(readOnly = true)
    public UsuarioDTO executar(String email) {
        // Busca usuário no banco
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "Usuário não encontrado: " + email
                ));

        // Converte para DTO (sem senha)
        return usuarioMapper.toDTO(usuario);
    }
}