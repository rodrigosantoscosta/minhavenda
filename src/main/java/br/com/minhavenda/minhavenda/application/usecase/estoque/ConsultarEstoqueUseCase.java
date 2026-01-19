package br.com.minhavenda.minhavenda.application.usecase.estoque;

import br.com.minhavenda.minhavenda.domain.entity.Produto;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.ProdutoRepository;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use Case para consultar estoque de um produto.
 *
 * Responsabilidades:
 * - Validar se produto existe
 * - Retornar quantidade em estoque
 */
@Service
@RequiredArgsConstructor
public class ConsultarEstoqueUseCase {

    private final ProdutoRepository produtoRepository;

    /**
     * Consulta estoque de um produto.
     *
     * @param produtoId ID do produto
     * @return quantidade em estoque (0 se null)
     * @throws RuntimeException se produto não encontrado
     */
    public Integer executar(UUID produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + produtoId));

        return produto.getEstoque() != null ? produto.getEstoque().getQuantidade() : 0;
    }
}