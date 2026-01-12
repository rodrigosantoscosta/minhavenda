package br.com.minhavenda.minhavenda.infrastructure.persistence.repository;

import br.com.minhavenda.minhavenda.domain.entity.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository para operações com Produto.
 *
 * Estende JpaSpecificationExecutor para suportar queries dinâmicas e seguras.
 */
@Repository
public interface ProdutoRepository extends JpaRepository<Produto, UUID>, JpaSpecificationExecutor<Produto> {

    /**
     * Busca apenas produtos ativos.
     */
    List<Produto> findByAtivoTrue();

    /**
     * Busca produtos ativos com paginação.
     */
    Page<Produto> findByAtivoTrue(Pageable pageable);

    /**
     * Busca produto por ID apenas se ativo.
     */
    Optional<Produto> findByIdAndAtivoTrue(UUID id);

    /**
     * Busca produtos por termo no nome ou descrição (apenas ativos).
     *
     * Usa @Query com parâmetros nomeados para segurança.
     */
    @Query("""
        SELECT p FROM Produto p
        WHERE p.ativo = true
        AND (LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%'))
        OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :termo, '%')))
        """)
    Page<Produto> buscarPorTermo(@Param("termo") String termo, Pageable pageable);

    /**
     * Busca produtos por categoria (apenas ativos).
     */
    @Query("""
        SELECT p FROM Produto p
        WHERE p.ativo = true
        AND p.categoria.id = :categoriaId
        """)
    Page<Produto> buscarPorCategoria(@Param("categoriaId") Long categoriaId, Pageable pageable);

    /**
     * Busca produtos por faixa de preço (apenas ativos).
     */
    @Query("""
        SELECT p FROM Produto p
        WHERE p.ativo = true
        AND p.preco BETWEEN :precoMin AND :precoMax
        """)
    Page<Produto> buscarPorFaixaPreco(
            @Param("precoMin") BigDecimal precoMin,
            @Param("precoMax") BigDecimal precoMax,
            Pageable pageable
    );

    /**
     * Conta produtos por categoria.
     */
    @Query("SELECT COUNT(p) FROM Produto p WHERE p.categoria.id = :categoriaId AND p.ativo = true")
    long countByCategoria(@Param("categoriaId") Long categoriaId);

    /**
     * Busca produtos com estoque baixo.
     */
    @Query("""
        SELECT p FROM Produto p
        WHERE p.ativo = true
        AND p.estoque <= :limiteEstoque
        ORDER BY p.estoque ASC
        """)
    List<Produto> buscarComEstoqueBaixo(@Param("limiteEstoque") Integer limiteEstoque);

    /**
     * Verifica se existe produto com o nome (case-insensitive).
     */
    boolean existsByNomeIgnoreCase(String nome);

    /**
     * Verifica se existe outro produto com o mesmo nome (para update).
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Produto p WHERE LOWER(p.nome) = LOWER(:nome) AND p.id <> :id")
    boolean existsByNomeAndIdNot(@Param("nome") String nome, @Param("id") UUID id);

    /**
     * Verifica se existem produtos ativos associados a uma categoria.
     * @param categoriaId ID da categoria a ser verificada
     * @return true se existirem produtos ativos associados à categoria, false caso contrário
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Produto p WHERE p.categoria.id = :categoriaId AND p.ativo = true")
    boolean existsByCategoriaId(@Param("categoriaId") Long categoriaId);
}