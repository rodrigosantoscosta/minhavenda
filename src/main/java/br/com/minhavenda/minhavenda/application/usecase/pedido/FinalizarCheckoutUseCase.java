package br.com.minhavenda.minhavenda.application.usecase.pedido;

import br.com.minhavenda.minhavenda.application.dto.pedido.CheckoutRequest;
import br.com.minhavenda.minhavenda.application.event.DomainEventPublisher;
import br.com.minhavenda.minhavenda.domain.entity.*;
import br.com.minhavenda.minhavenda.domain.enums.StatusCarrinho;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.CarrinhoRepository;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.PedidoRepository;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.ProdutoRepository;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Use Case: Finalizar Checkout
 *
 * Converte o carrinho ativo do usuário em um pedido.
 *
 * Fluxo:
 * 1. Busca usuário
 * 2. Busca carrinho ativo
 * 3. Valida carrinho não vazio
 * 4. Valida estoque dos produtos
 * 5. Cria pedido
 * 6. Copia itens do carrinho para o pedido
 * 7. Atualiza estoque dos produtos
 * 8. Finaliza carrinho
 * 9. Salva pedido (gera ID)
 * 10. Registra evento de criação
 * 11. Publica eventos
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinalizarCheckoutUseCase {

    private final CarrinhoRepository carrinhoRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final DomainEventPublisher eventPublisher;

    /**
     * Executa o checkout.
     *
     * @param usuarioId ID do usuário
     * @param request dados do checkout (endereço, observações)
     * @return pedido criado
     * @throws RuntimeException se validação falhar
     */
    @Transactional
    public Pedido executar(UUID usuarioId, CheckoutRequest request) {
        log.info("Iniciando checkout para usuário: {}", usuarioId);

        // 1. Buscar usuário
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 2. Buscar carrinho ativo
        Carrinho carrinho = carrinhoRepository
                .findByUsuarioAndStatus(usuario, StatusCarrinho.ATIVO)
                .orElseThrow(() -> new RuntimeException("Carrinho não encontrado"));

        // 3. Validar carrinho
        if (carrinho.getItens().isEmpty()) {
            throw new RuntimeException("Carrinho está vazio");
        }

        // 4. Validar estoque
        validarEstoque(carrinho);

        // 5. Criar pedido com valores ZERADOS (serão calculados depois)
        BigDecimal valorFrete = BigDecimal.ZERO; // TODO: calcular frete
        BigDecimal valorDesconto = BigDecimal.ZERO; // TODO: aplicar cupom

        Pedido pedido = new Pedido(
                usuario,
                BigDecimal.ZERO,
                valorFrete,
                valorDesconto,
                BigDecimal.ZERO,
                request.getEnderecoEntrega(),
                request.getObservacoes()
        );

        // 6. Copiar itens do carrinho ANTES de salvar
        for (ItemCarrinho itemCarrinho : carrinho.getItens()) {
            pedido.adicionarItem(
                    itemCarrinho.getProduto(),
                    itemCarrinho.getQuantidade(),
                    itemCarrinho.getPrecoUnitario()
            );
        }

        // 7. Atualizar estoque dos produtos
        for (ItemCarrinho itemCarrinho : carrinho.getItens()) {
            Produto produto = itemCarrinho.getProduto();
            produto.removerEstoque(itemCarrinho.getQuantidade());
            produtoRepository.save(produto);
        }

        // 8. Finalizar carrinho
        carrinho.finalizar();
        carrinhoRepository.save(carrinho);

        // 9. Log antes de salvar (para debug)
        log.info("=== ANTES DE SALVAR ===");
        log.info("Quantidade de itens: {}", pedido.getItens().size());
        log.info("Subtotal: {}", pedido.getSubtotal());
        log.info("Valor Total: {}", pedido.getValorTotal());

        // 10. Salvar pedido
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        log.info("Pedido criado: {} - Valor: R$ {}",
                pedidoSalvo.getId(), pedidoSalvo.getValorTotal());

        // 11. Registrar evento de criação
        pedidoSalvo.registrarCriacao();

        // 12. Publicar eventos
        eventPublisher.publishAll(pedidoSalvo.getDomainEvents());
        pedidoSalvo.limparEventos();

        log.info("Checkout finalizado com sucesso!");

        return pedidoSalvo;
    }

    /**
     * Valida que todos os produtos têm estoque suficiente.
     *
     * @param carrinho carrinho a validar
     * @throws RuntimeException se estoque insuficiente
     */
    private void validarEstoque(Carrinho carrinho) {
        for (ItemCarrinho item : carrinho.getItens()) {
            Produto produto = item.getProduto();
            Integer quantidadeNecessaria = item.getQuantidade();

            // Valida produto ativo
            if (!produto.getAtivo()) {
                throw new RuntimeException(
                        String.format("Produto '%s' não está mais disponível",
                                produto.getNome())
                );
            }

            // Valida estoque suficiente
            if (!produto.temEstoqueSuficiente(quantidadeNecessaria)) {
                throw new RuntimeException(
                        String.format(
                                "Estoque insuficiente para '%s'. Disponível: %d, Solicitado: %d",
                                produto.getNome(),
                                produto.getEstoque().getQuantidade(),
                                quantidadeNecessaria
                        )
                );
            }
        }
    }
}