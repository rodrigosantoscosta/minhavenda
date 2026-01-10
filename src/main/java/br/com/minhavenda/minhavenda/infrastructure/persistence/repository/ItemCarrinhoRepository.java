package br.com.minhavenda.minhavenda.infrastructure.persistence.repository;

import br.com.minhavenda.minhavenda.domain.entity.Carrinho;
import br.com.minhavenda.minhavenda.domain.entity.ItemCarrinho;
import br.com.minhavenda.minhavenda.domain.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository para ItemCarrinho.
 */
@Repository
public interface ItemCarrinhoRepository extends JpaRepository<ItemCarrinho, UUID> {

    /**
     * Busca item do carrinho por produto.
     * Usado para verificar se produto já está no carrinho.
     * 
     * @param carrinho carrinho
     * @param produto produto
     * @return item se encontrado
     */
    Optional<ItemCarrinho> findByCarrinhoAndProduto(Carrinho carrinho, Produto produto);
}
