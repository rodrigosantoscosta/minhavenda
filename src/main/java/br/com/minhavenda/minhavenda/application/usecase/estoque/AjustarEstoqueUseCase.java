//package br.com.minhavenda.minhavenda.application.usecase.estoque;
//
//
//import br.com.minhavenda.minhavenda.domain.entity.Produto;
//import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.ProdutoRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.UUID;
//
///**
// * Use Case para ajustar estoque de um produto (definir quantidade exata).
// *
// * Responsabilidades:
// * - Validar se produto existe
// * - Validar quantidade não-negativa
// * - Definir quantidade exata de estoque
// * - Registrar log da operação
// */
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class AjustarEstoqueUseCase {
//
//    private final ProdutoRepository produtoRepository;
//
//    /**
//     * Ajusta estoque de um produto para quantidade exata.
//     *
//     * @param produtoId ID do produto
//     * @param novaQuantidade nova quantidade em estoque (deve ser >= 0)
//     * @param motivo motivo do ajuste (opcional)
//     * @throws RuntimeException se produto não encontrado
//     * @throws IllegalArgumentException se quantidade negativa
//     */
//    @Transactional
//    public void executar(UUID produtoId, Integer novaQuantidade, String motivo) {
//        log.info("Ajustando estoque do produto {} para {}", produtoId, novaQuantidade);
//
//        // Validar quantidade
//        if (novaQuantidade == null || novaQuantidade < 0) {
//            throw new IllegalArgumentException("Quantidade não pode ser negativa");
//        }
//
//        // Buscar produto
//        Produto produto = produtoRepository.findById(produtoId)
//                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + produtoId));
//
//        Integer estoqueAnterior = produto.getEstoque().getQuantidade() != null ? produto.getEstoque().getQuantidade() : 0;
//
//        // Ajustar estoque
//        produto.adicionarEstoque(novaQuantidade);
//
//        // Salvar
//        produtoRepository.save(produto);
//
//        log.info("Estoque ajustado. Produto {}: {} → {}. Motivo: {}",
//                produtoId, estoqueAnterior, novaQuantidade,
//                motivo != null ? motivo : "Não informado");
//    }
//}