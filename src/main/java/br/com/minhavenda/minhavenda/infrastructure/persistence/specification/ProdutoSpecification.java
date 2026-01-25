package br.com.minhavenda.minhavenda.infrastructure.persistence.specification;

import br.com.minhavenda.minhavenda.application.dto.produto.FiltroProdutoRequest;
import br.com.minhavenda.minhavenda.domain.entity.Produto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Specification para construir queries seguras de busca de produtos.
 *
 * Usa JPA Criteria API para construir query segura.
 * Todos os parâmetros são passados como prepared statements.
 */
public class ProdutoSpecification {

    /**
     * Cria Specification baseada nos filtros fornecidos.
     *
     * @param filtro Filtros de busca
     * @return Specification para usar em ProdutoRepository.findAll(spec)
     */
    public static Specification<Produto> comFiltros(FiltroProdutoRequest filtro) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro: Termo de busca (nome OU descrição)
            if (filtro.getTermo() != null && !filtro.getTermo().trim().isEmpty()) {
                String termoLike = "%" + filtro.getTermoNormalizado() + "%";

                Predicate nomePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")),
                        termoLike
                );

                Predicate descricaoPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("descricao")),
                        termoLike
                );

                // OR: nome LIKE '%termo%' OR descricao LIKE '%termo%'
                predicates.add(criteriaBuilder.or(nomePredicate, descricaoPredicate));
            }

            // Filtro: Categoria
            if (filtro.getCategoriaId() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("categoria").get("id"), filtro.getCategoriaId())
                );
            }

            // Filtro: Preço mínimo
            if (filtro.getPrecoMin() != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("preco"), filtro.getPrecoMin())
                );
            }

            // Filtro: Preço máximo
            if (filtro.getPrecoMax() != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(root.get("preco"), filtro.getPrecoMax())
                );
            }

            // Filtro: Ativo
            if (filtro.getAtivo() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("ativo"), filtro.getAtivo())
                );
            }

            // Combina todos os predicates com AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification para buscar apenas produtos ativos.
     */
    public static Specification<Produto> apenasAtivos() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("ativo"), true);
    }

    /**
     * Specification para buscar por termo no nome ou descrição.
     */
    public static Specification<Produto> porTermo(String termo) {
        return (root, query, criteriaBuilder) -> {
            if (termo == null || termo.trim().isEmpty()) {
                return criteriaBuilder.conjunction(); // Sem filtro
            }

            String termoLike = "%" + termo.trim().toLowerCase() + "%";

            Predicate nomePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("nome")),
                    termoLike
            );

            Predicate descricaoPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("descricao")),
                    termoLike
            );

            return criteriaBuilder.or(nomePredicate, descricaoPredicate);
        };
    }

    /**
     * Specification para buscar por categoria.
     */
    public static Specification<Produto> porCategoria(Long categoriaId) {
        return (root, query, criteriaBuilder) -> {
            if (categoriaId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("categoria").get("id"), categoriaId);
        };
    }

    /**
     * Specification para buscar por faixa de preço.
     */
    public static Specification<Produto> porFaixaPreco(java.math.BigDecimal precoMin, java.math.BigDecimal precoMax) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (precoMin != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("preco"), precoMin)
                );
            }

            if (precoMax != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(root.get("preco"), precoMax)
                );
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}