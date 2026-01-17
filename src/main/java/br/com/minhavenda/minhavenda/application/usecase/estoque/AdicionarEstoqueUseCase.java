package br.com.minhavenda.minhavenda.application.usecase.estoque;


import br.com.minhavenda.minhavenda.domain.entity.Produto;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use Case para adicionar estoque de um produto.
 *
 * Responsabilidades:
 * - Validar se produto existe
 * - Validar quantidade positiva
 * - Adicionar quantidade ao estoque
 * - Registrar log da operação
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdicionarEstoqueUseCase {

    private final ProdutoRepository produtoRepository;

    /**
     * Adiciona estoque de um produto.
     *
     * @param produtoId ID do produto
     * @param quantidade quantidade a adicionar (deve ser > 0)
     * @param motivo motivo da adição (opcional)
     * @throws RuntimeException se produto não encontrado
     * @throws IllegalArgumentException se quantidade inválida
     */
    @Transactional
    public void executar(UUID produtoId, Integer quantidade, String motivo) {
        log.info("Adicionando {} unidades ao estoque do produto {}", quantidade, produtoId);

        // Validar quantidade
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        // Buscar produto
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + produtoId));

        // Obter estoque atual (tratar null como 0)
        Integer estoqueAtual = produto.getEstoque().getQuantidade() != null ? produto.getEstoque().getQuantidade() : 0;

        // Adicionar quantidade
        Integer novoEstoque = estoqueAtual + quantidade;
        produto.adicionarEstoque(quantidade);

        // Salvar
        produtoRepository.save(produto);

        log.info("Estoque atualizado. Produto {}: {} → {}. Motivo: {}",
                produtoId, estoqueAtual, novoEstoque, motivo != null ? motivo : "Não informado");
    }
}