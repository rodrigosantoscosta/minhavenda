package br.com.minhavenda.minhavenda.infrastructure.persistence.repository;

import br.com.minhavenda.minhavenda.domain.entity.Carrinho;
import br.com.minhavenda.minhavenda.domain.entity.Usuario;
import br.com.minhavenda.minhavenda.domain.enums.StatusCarrinho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository para Carrinho.
 */
@Repository
public interface CarrinhoRepository extends JpaRepository<Carrinho, UUID> {

    /**
     * Busca carrinho do usuário com status específico.
     *
     * @param usuario usuário
     * @param status status do carrinho
     * @return carrinho se encontrado
     */
    Optional<Carrinho> findByUsuarioAndStatus(Usuario usuario, StatusCarrinho status);
}