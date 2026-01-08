package br.com.minhavenda.minhavenda.infrastructure.persistence.repository;

import br.com.minhavenda.minhavenda.domain.entity.Usuario;
import br.com.minhavenda.minhavenda.domain.valueobject.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository para Usuario.
 * 
 * Métodos necessários para autenticação:
 * - findByEmail: buscar usuário por email (login)
 * - existsByEmail: validar email único (cadastro)
 * - existsByCpf: validar CPF único (cadastro)
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    /**
     * Busca usuário por email.
     * Usado no login e ao carregar UserDetails.
     * 
     * @param email do usuário
     * @return Optional com usuário se encontrado
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica se existe usuário com determinado email.
     * Usado no cadastro para garantir email único.
     * 
     * @param email a verificar
     * @return true se existe, false caso contrário
     */
    boolean existsByEmail(String email);

    /**
     * Verifica se existe usuário com determinado CPF.
     * Usado no cadastro para garantir CPF único.
     * 
     * @param cpf CPF a verificar (sem máscara)
     * @return true se existe, false caso contrário
     */
//    boolean existsByCpf(String cpf);
}
