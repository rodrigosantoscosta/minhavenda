package br.com.minhavenda.minhavenda.application.dto.produto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para filtros de busca de produtos.
 *
 * Filtros disponíveis:
 * - termo: busca no nome e descrição do produto
 * - categoriaId: filtra por ID da categoria
 * - precoMin: preço mínimo (inclusivo)
 * - precoMax: preço máximo (inclusivo)
 * - ativo: filtra por status ativo/inativo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltroProdutoRequest {

    /**
     * Termo de busca para nome e descrição.
     * Limitado a 100 caracteres por segurança.
     */
    @Size(max = 100, message = "Termo de busca deve ter no máximo 100 caracteres")
    private String termo;

    /**
     * ID da categoria para filtrar.
     * Usa Long pois a entidade Categoria usa Long como ID.
     */
    private Long categoriaId;

    /**
     * Preço mínimo (inclusivo).
     * Deve ser >= 0.
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "Preço mínimo deve ser maior ou igual a zero")
    private BigDecimal precoMin;

    /**
     * Preço máximo (inclusivo).
     * Deve ser >= 0.
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "Preço máximo deve ser maior ou igual a zero")
    private BigDecimal precoMax;

    /**
     * Filtrar por status ativo/inativo.
     * Padrão: true (apenas ativos).
     * Null = busca em todos (ativos e inativos).
     */
    private Boolean ativo = true;

    /**
     * Valida se precoMin <= precoMax.
     */
    public boolean isPrecoRangeValido() {
        if (precoMin != null && precoMax != null) {
            return precoMin.compareTo(precoMax) <= 0;
        }
        return true;
    }

    /**
     * Verifica se algum filtro foi aplicado.
     */
    public boolean hasAnyFilter() {
        return termo != null ||
                categoriaId != null ||
                precoMin != null ||
                precoMax != null;
    }

    /**
     * Normaliza o termo de busca (remove espaços extras, lowercase).
     */
    public String getTermoNormalizado() {
        if (termo == null) {
            return null;
        }
        return termo.trim().toLowerCase();
    }
}