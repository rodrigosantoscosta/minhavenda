package br.com.minhavenda.minhavenda.infrastructure.persistence.repository;

import br.com.minhavenda.minhavenda.domain.entity.Carrinho;
import br.com.minhavenda.minhavenda.domain.entity.Usuario;
import br.com.minhavenda.minhavenda.domain.enums.StatusCarrinho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para gerenciamento de Carrinho.
 *
 * Provê acesso aos dados de carrinhos no banco de dados.
 */
@Repository
public interface CarrinhoRepository extends JpaRepository<Carrinho, UUID> {

    /**
     * Busca carrinho do usuário com status específico.
     *
     * Método preferencial para uso no sistema.
     * Usado por PedidoService e outros serviços.
     *
     * @param usuario usuário dono do carrinho
     * @param status status do carrinho (ATIVO, FINALIZADO, etc.)
     * @return Optional contendo o carrinho, ou empty se não encontrado
     */
    Optional<Carrinho> findByUsuarioAndStatus(Usuario usuario, StatusCarrinho status);

    /**
     * Busca carrinho do usuário com status específico usando ID do usuário.
     *
     * Método alternativo para casos onde só temos o ID do usuário.
     *
     * @param usuarioId ID do usuário
     * @param status status do carrinho
     * @return Optional contendo o carrinho, ou empty se não encontrado
     */
    Optional<Carrinho> findByUsuarioIdAndStatus(UUID usuarioId, StatusCarrinho status);
}