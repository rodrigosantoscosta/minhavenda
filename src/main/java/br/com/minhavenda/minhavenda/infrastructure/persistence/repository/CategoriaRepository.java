package br.com.minhavenda.minhavenda.infrastructure.persistence.repository;

import br.com.minhavenda.minhavenda.domain.entity.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {


    /**
     * Busca categorias ativas.
     */
    List<Categoria> findByAtivoTrue();

    /**
     * Busca categorias ativas com paginação.
     */
    Page<Categoria> findByAtivoTrue(Pageable pageable);

    /**
     * Busca categoria por nome (ignora case).
     */
    Optional<Categoria> findByNomeIgnoreCase(String nome);

    // ========================================
    // CONSULTAS DE EXISTÊNCIA
    // ========================================

    /**
     * Verifica se existe categoria com determinado nome (ignora case).
     */
    boolean existsByNomeIgnoreCase(String nome);

    /**
     * Verifica se existe categoria com nome, excluindo um ID específico.
     * Útil ao editar categoria (evitar duplicidade).
     */
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    // ========================================
    // CONSULTAS CUSTOMIZADAS
    // ========================================

    /**
     * Busca categorias por termo no nome ou descrição.
     */
    @Query("SELECT c FROM Categoria c WHERE " +
            "LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "LOWER(c.descricao) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<Categoria> buscarPorTermo(@Param("termo") String termo);

    /**
     * Busca categorias ativas por termo.
     */
    @Query("SELECT c FROM Categoria c WHERE " +
            "c.ativo = true AND " +
            "(LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "LOWER(c.descricao) LIKE LOWER(CONCAT('%', :termo, '%')))")
    List<Categoria> buscarAtivosPorTermo(@Param("termo") String termo);

    /**
     * Conta quantas categorias ativas existem.
     */
    long countByAtivoTrue();
}