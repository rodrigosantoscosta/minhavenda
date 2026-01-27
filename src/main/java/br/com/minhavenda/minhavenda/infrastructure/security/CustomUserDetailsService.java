package br.com.minhavenda.minhavenda.infrastructure.security;

import br.com.minhavenda.minhavenda.domain.entity.Usuario;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Serviço customizado para carregar detalhes do usuário.
 * 
 * Implementa UserDetailsService do Spring Security.
 * 
 * Responsabilidades:
 * - Carregar usuário do banco pelo email
 * - Converter entidade Usuario para UserDetails (formato do Spring Security)
 * - Definir authorities (roles/permissions)
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carrega usuário pelo email (username).
     * 
     * Chamado pelo Spring Security ao validar credenciais.
     * 
     * @param  email do usuário
     * @return UserDetails com dados do usuário
     * @throws UsernameNotFoundException se usuário não existe
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        // 1. Busca usuário no banco pelo email
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado com email: " + email
                ));

        // 2. Verifica se usuário está ativo
        if (!usuario.getAtivo()) {
            throw new UsernameNotFoundException("Usuário inativo: " + email);
        }

        // 3. Converte entidade Usuario para UserDetails
        // UserDetails é a interface que o Spring Security entende
        return User.builder()
                // Username = email
                .username(usuario.getEmail())
                
                // Senha hasheada (BCrypt)
                .password(usuario.getSenha())
                
                // Authorities (roles/permissions)
                // Formato: "ROLE_USER", "ROLE_ADMIN", etc
                .authorities(Collections.singleton(
                        new SimpleGrantedAuthority("ROLE_" + usuario.getTipo().name())
                ))
                
                // Flags de conta
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!usuario.getAtivo())
                
                .build();
    }
}
