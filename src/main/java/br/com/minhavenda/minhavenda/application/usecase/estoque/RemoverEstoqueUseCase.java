package br.com.minhavenda.minhavenda.application.usecase.estoque;

import br.com.minhavenda.minhavenda.domain.entity.Produto;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use Case para remover estoque de um produto.
 *
 * Responsabilidades:
 * - Validar se produto existe
 * - Validar quantidade positiva
 * - Validar se há estoque suficiente
 * - Remover quantidade do estoque
 * - Registrar log da operação
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RemoverEstoqueUseCase {

    private final ProdutoRepository produtoRepository;

    /**
     * Remove estoque de um produto.
     *
     * @param produtoId ID do produto
     * @param quantidade quantidade a remover (deve ser > 0)
     * @param motivo motivo da remoção (opcional)
     * @throws RuntimeException se produto não encontrado
     * @throws IllegalArgumentException se quantidade inválida ou estoque insuficiente
     */
    @Transactional
    public void executar(UUID produtoId, Integer quantidade, String motivo) {
        log.info("Removendo {} unidades do estoque do produto {}", quantidade, produtoId);

        // Validar quantidade
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        // Buscar produto
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + produtoId));

        // Obter estoque atual
        Integer estoqueAtual = produto.getEstoque().getQuantidade() != null ? produto.getEstoque().getQuantidade() : 0;

        // Validar estoque suficiente
        if (estoqueAtual < quantidade) {
            throw new IllegalArgumentException(
                    String.format("Estoque insuficiente. Disponível: %d, Solicitado: %d",
                            estoqueAtual, quantidade)
            );
        }

        // Remover quantidade
        Integer novoEstoque = estoqueAtual - quantidade;
        produto.removerEstoque(quantidade);

        // Salvar
        produtoRepository.save(produto);

        log.info("Estoque atualizado. Produto {}: {} → {}. Motivo: {}",
                produtoId, estoqueAtual, novoEstoque, motivo != null ? motivo : "Não informado");
    }
}