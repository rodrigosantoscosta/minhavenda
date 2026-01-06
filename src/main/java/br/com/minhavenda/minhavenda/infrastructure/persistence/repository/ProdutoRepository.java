package br.com.minhavenda.minhavenda.infrastructure.persistence.repository;

import br.com.minhavenda.minhavenda.domain.entity.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository para acesso aos dados da entidade Produto.
 *
 * Este Repository faz parte da camada de INFRASTRUCTURE seguindo DDD e Clean Architecture:
 * - NÃO contém regras de negócio (ficam na camada Domain)
 * - NÃO contém lógica de aplicação (fica nos Use Cases)
 * - APENAS acessa e persiste dados no banco
 *
 * @author MinhaVenda Team
 * @since 1.0.0
 */
@Repository
public interface ProdutoRepository extends JpaRepository<Produto, UUID> {

    // ========================================
    // CONSULTAS POR ATRIBUTOS BÁSICOS
    // ========================================

    /**
     * Busca produtos ativos ou inativos.
     *
     * @param ativo true para produtos ativos, false para inativos
     * @return Lista de produtos filtrados por status
     */
    List<Produto> findByAtivo(Boolean ativo);

    /**
     * Busca produtos ativos ou inativos com paginação.
     *
     * @param ativo true para produtos ativos, false para inativos
     * @param pageable configurações de paginação
     * @return Página de produtos
     */
    Page<Produto> findByAtivo(Boolean ativo, Pageable pageable);

    /**
     * Busca produto por nome exato (case-sensitive).
     *
     * @param nome nome do produto
     * @return Optional com produto se encontrado
     */
    Optional<Produto> findByNome(String nome);

    /**
     * Busca produtos por nome ignorando maiúsculas/minúsculas.
     *
     * @param nome nome do produto (case-insensitive)
     * @return Lista de produtos encontrados
     */
    List<Produto> findByNomeIgnoreCase(String nome);

    // ========================================
    // CONSULTAS POR CATEGORIA
    // ========================================

    /**
     * Busca produtos de uma categoria específica.
     *
     * @param categoriaId ID da categoria
     * @return Lista de produtos da categoria
     */
    List<Produto> findByCategoriaId(Long categoriaId);

    /**
     * Busca produtos de uma categoria com paginação.
     *
     * @param categoriaId ID da categoria
     * @param pageable configurações de paginação
     * @return Página de produtos
     */
    Page<Produto> findByCategoriaId(Long categoriaId, Pageable pageable);

    /**
     * Busca produtos ativos de uma categoria específica.
     *
     * @param categoriaId ID da categoria
     * @param ativo status do produto
     * @return Lista de produtos
     */
    List<Produto> findByCategoriaIdAndAtivo(Long categoriaId, Boolean ativo);

    /**
     * Busca produtos ativos de uma categoria com paginação.
     *
     * @param categoriaId ID da categoria
     * @param ativo status do produto
     * @param pageable configurações de paginação
     * @return Página de produtos
     */
    Page<Produto> findByCategoriaIdAndAtivo(Long categoriaId, Boolean ativo, Pageable pageable);

    // ========================================
    // CONSULTAS POR PREÇO
    // ========================================

    /**
     * Busca produtos com preço menor ou igual ao valor especificado.
     *
     * @param preco preço máximo
     * @return Lista de produtos
     */
    List<Produto> findByPrecoLessThanEqual(BigDecimal preco);

    /**
     * Busca produtos com preço maior ou igual ao valor especificado.
     *
     * @param preco preço mínimo
     * @return Lista de produtos
     */
    List<Produto> findByPrecoGreaterThanEqual(BigDecimal preco);

    /**
     * Busca produtos dentro de uma faixa de preço.
     *
     * @param precoMin preço mínimo
     * @param precoMax preço máximo
     * @return Lista de produtos
     */
    List<Produto> findByPrecoBetween(BigDecimal precoMin, BigDecimal precoMax);

    /**
     * Busca produtos ativos dentro de uma faixa de preço com paginação.
     *
     * @param precoMin preço mínimo
     * @param precoMax preço máximo
     * @param ativo status do produto
     * @param pageable configurações de paginação
     * @return Página de produtos
     */
    Page<Produto> findByPrecoBetweenAndAtivo(BigDecimal precoMin, BigDecimal precoMax, Boolean ativo, Pageable pageable);

    // ========================================
    // BUSCAS TEXTUAIS (LIKE)
    // ========================================

    /**
     * Busca produtos por parte do nome (LIKE %termo%).
     *
     * @param nome parte do nome a buscar
     * @return Lista de produtos encontrados
     */
    List<Produto> findByNomeContainingIgnoreCase(String nome);

    /**
     * Busca produtos por parte do nome com paginação.
     *
     * @param nome parte do nome a buscar
     * @param pageable configurações de paginação
     * @return Página de produtos
     */
    Page<Produto> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    /**
     * Busca produtos por parte da descrição.
     *
     * @param descricao parte da descrição a buscar
     * @return Lista de produtos encontrados
     */
    List<Produto> findByDescricaoContainingIgnoreCase(String descricao);

    // ========================================
    // CONSULTAS CUSTOMIZADAS (JPQL)
    // ========================================

    /**
     * Busca produtos por nome OU descrição (busca textual ampla).
     * Útil para implementar funcionalidade de "buscar produtos".
     *
     * @param termo termo de busca
     * @param pageable configurações de paginação
     * @return Página de produtos que contêm o termo no nome ou descrição
     */
    @Query("SELECT p FROM Produto p WHERE " +
           "LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(p.descricao) LIKE LOWER(CONCAT('%', :termo, '%'))")
    Page<Produto> buscarPorTexto(@Param("termo") String termo, Pageable pageable);

    /**
     * Busca produtos ativos por nome OU descrição.
     *
     * @param termo termo de busca
     * @param ativo status do produto
     * @param pageable configurações de paginação
     * @return Página de produtos ativos encontrados
     */
    @Query("SELECT p FROM Produto p WHERE " +
           "p.ativo = :ativo AND " +
           "(LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(p.descricao) LIKE LOWER(CONCAT('%', :termo, '%')))")
    Page<Produto> buscarPorTextoEStatus(@Param("termo") String termo,
                                         @Param("ativo") Boolean ativo,
                                         Pageable pageable);

    /**
     * Busca produtos com filtros múltiplos (categoria, faixa de preço, status).
     * Query complexa para tela de listagem com filtros.
     *
     * @param categoriaId ID da categoria (opcional)
     * @param precoMin preço mínimo (opcional)
     * @param precoMax preço máximo (opcional)
     * @param ativo status do produto
     * @param pageable configurações de paginação
     * @return Página de produtos filtrados
     */
    @Query("SELECT p FROM Produto p WHERE " +
           "(:categoriaId IS NULL OR p.categoria.id = :categoriaId) AND " +
           "(:precoMin IS NULL OR p.preco >= :precoMin) AND " +
           "(:precoMax IS NULL OR p.preco <= :precoMax) AND " +
           "p.ativo = :ativo")
    Page<Produto> buscarComFiltros(@Param("categoriaId") UUID categoriaId,
                                    @Param("precoMin") BigDecimal precoMin,
                                    @Param("precoMax") BigDecimal precoMax,
                                    @Param("ativo") Boolean ativo,
                                    Pageable pageable);

    /**
     * Busca produtos relacionados (mesma categoria, excluindo o próprio produto).
     * Útil para seção "Produtos Relacionados" na página de detalhe.
     *
     * @param categoriaId ID da categoria
     * @param produtoId ID do produto a excluir
     * @param pageable configurações de paginação (limite)
     * @return Página de produtos relacionados
     */
    @Query("SELECT p FROM Produto p WHERE " +
           "p.categoria.id = :categoriaId AND " +
           "p.id != :produtoId AND " +
           "p.ativo = true")
    Page<Produto> buscarProdutosRelacionados(@Param("categoriaId") UUID categoriaId,
                                              @Param("produtoId") UUID produtoId,
                                              Pageable pageable);

    // ========================================
    // CONSULTAS DE AGREGAÇÃO
    // ========================================

    /**
     * Conta quantos produtos ativos existem.
     *
     * @param ativo status do produto
     * @return quantidade de produtos
     */
    long countByAtivo(Boolean ativo);

    /**
     * Conta quantos produtos existem em uma categoria.
     *
     * @param categoriaId ID da categoria
     * @return quantidade de produtos na categoria
     */
    long countByCategoriaId(Long categoriaId);

    /**
     * Conta produtos ativos por categoria.
     *
     * @param categoriaId ID da categoria
     * @param ativo status do produto
     * @return quantidade de produtos
     */
    long countByCategoriaIdAndAtivo(Long categoriaId, Boolean ativo);

    // ========================================
    // VERIFICAÇÕES DE EXISTÊNCIA
    // ========================================

    /**
     * Verifica se existe produto com determinado nome.
     * Útil para validar duplicidade.
     *
     * @param nome nome do produto
     * @return true se existe
     */
    boolean existsByNome(String nome);

    /**
     * Verifica se existe produto com determinado nome, excluindo um ID específico.
     * Útil ao editar produto (evitar duplicidade, mas permitir mesmo nome do produto sendo editado).
     *
     * @param nome nome do produto
     * @param id ID do produto a excluir da verificação
     * @return true se existe outro produto com mesmo nome
     */
    boolean existsByNomeAndIdNot(String nome, UUID id);

    /**
     * Verifica se uma categoria possui produtos.
     * Útil antes de deletar categoria.
     *
     * @param categoriaId ID da categoria
     * @return true se categoria possui produtos
     */
    boolean existsByCategoriaId(Long categoriaId);

    // ========================================
    // CONSULTAS NATIVAS (SQL)
    // ========================================

    /**
     * Busca os N produtos mais recentes.
     *
     * @param limite quantidade de produtos
     * @return Lista de produtos mais recentes
     */
    @Query(value = "SELECT * FROM produtos WHERE ativo = true " +
                   "ORDER BY data_cadastro DESC LIMIT :limite",
           nativeQuery = true)
    List<Produto> buscarMaisRecentes(@Param("limite") int limite);

    /**
     * Busca produtos por faixa de preço usando query nativa.
     * Exemplo de uso de SQL nativo quando necessário.
     *
     * @param precoMin preço mínimo
     * @param precoMax preço máximo
     * @return Lista de produtos
     */
    @Query(value = "SELECT * FROM produtos " +
                   "WHERE preco BETWEEN :precoMin AND :precoMax " +
                   "AND ativo = true " +
                   "ORDER BY preco ASC",
           nativeQuery = true)
    List<Produto> buscarPorFaixaPreco(@Param("precoMin") BigDecimal precoMin,
                                       @Param("precoMax") BigDecimal precoMax);
}
