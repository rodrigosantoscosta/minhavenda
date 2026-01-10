package br.com.minhavenda.minhavenda.application.service;

import br.com.minhavenda.minhavenda.application.dto.pedido.CheckoutRequest;
import br.com.minhavenda.minhavenda.application.dto.pedido.PedidoDTO;
import br.com.minhavenda.minhavenda.application.dto.pedido.PedidoDetalhadoDTO;
import br.com.minhavenda.minhavenda.application.mapper.PedidoMapper;
import br.com.minhavenda.minhavenda.domain.entity.*;
import br.com.minhavenda.minhavenda.domain.enums.StatusCarrinho;
import br.com.minhavenda.minhavenda.domain.enums.StatusPedido;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service para gerenciar pedidos.
 *
 * Responsabilidades:
 * - Checkout: converter carrinho em pedido
 * - Listar pedidos do usuário
 * - Buscar pedido específico
 * - Gerenciar status do pedido (pagar, enviar, entregar, cancelar)
 * - Validações de negócio
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final PedidoMapper pedidoMapper;

    /**
     * Finaliza o checkout convertendo o carrinho em pedido.
     *
     * Fluxo:
     * 1. Busca carrinho ativo do usuário
     * 2. Valida que carrinho não está vazio
     * 3. Valida estoque de todos os produtos
     * 4. Cria pedido com status CRIADO
     * 5. Copia itens do carrinho para o pedido (snapshot)
     * 6. Finaliza carrinho (status FINALIZADO)
     * 7. Salva pedido
     *
     * @param email email do usuário (do token JWT)
     * @param request dados do checkout (endereço, observações)
     * @return pedido criado
     */
    @Transactional
    public PedidoDTO finalizarCheckout(String email, CheckoutRequest request) {
        // 1. Buscar usuário
        Usuario usuario = buscarUsuarioPorEmail(email);

        // 2. Buscar carrinho ativo
        Carrinho carrinho = carrinhoRepository
                .findByUsuarioAndStatus(usuario, StatusCarrinho.ATIVO)
                .orElseThrow(() -> new RuntimeException("Carrinho não encontrado"));

        // 3. Validar carrinho não vazio
        if (carrinho.getItens().isEmpty()) {
            throw new RuntimeException("Carrinho está vazio");
        }

        // 4. Validar estoque de todos os produtos
        validarEstoqueProdutos(carrinho);

        // 5. Criar pedido
        Pedido pedido = Pedido.builder()
                .usuario(usuario)
                .status(StatusPedido.CRIADO)
                .enderecoEntrega(request.getEnderecoEntrega())
                .observacoes(request.getObservacoes())
                .valorFrete(BigDecimal.ZERO)
                .valorDesconto(BigDecimal.ZERO)
                .build();

        // 6. Copiar itens do carrinho para o pedido (snapshot)
        for (ItemCarrinho itemCarrinho : carrinho.getItens()) {
            ItemPedido itemPedido = ItemPedido.builder()
                    .produto(itemCarrinho.getProduto())
                    .produtoNome(itemCarrinho.getProduto().getNome()) // Snapshot do nome
                    .quantidade(itemCarrinho.getQuantidade())
                    .precoUnitario(itemCarrinho.getPrecoUnitario()) // Snapshot do preço
                    .build();

            pedido.adicionarItem(itemPedido);
        }

        // 7. Calcular valores totais
        pedido.calcularValorTotal();

        // 8. Salvar pedido
        pedido = pedidoRepository.save(pedido);

        // 9. Finalizar carrinho (não deletar, manter histórico)
        carrinho.finalizar();
        carrinhoRepository.save(carrinho);

        log.info("Pedido criado: ID={}, Usuario={}, Valor={}, Itens={}",
                pedido.getId(),
                usuario.getEmail(),
                pedido.getValorTotal(),
                pedido.getItens().size());

        return pedidoMapper.toDTO(pedido);
    }

    /**
     * Lista todos os pedidos do usuário logado.
     * Ordenados por data de criação (mais recente primeiro).
     *
     * @param email email do usuário
     * @return lista de pedidos
     */
    @Transactional(readOnly = true)
    public List<PedidoDTO> listarMeusPedidos(String email) {
        Usuario usuario = buscarUsuarioPorEmail(email);

        List<Pedido> pedidos = pedidoRepository.findByUsuarioOrderByDataCriacaoDesc(usuario);

        return pedidos.stream()
                .map(pedidoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca pedido específico por ID.
     * Valida que o pedido pertence ao usuário logado.
     *
     * @param email email do usuário
     * @param pedidoId ID do pedido
     * @return pedido detalhado
     */
    @Transactional(readOnly = true)
    public PedidoDetalhadoDTO buscarPedido(String email, UUID pedidoId) {
        Usuario usuario = buscarUsuarioPorEmail(email);

        Pedido pedido = pedidoRepository.findByIdAndUsuario(pedidoId, usuario)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        return pedidoMapper.toDetalhadoDTO(pedido);
    }

    /**
     * Marca pedido como pago (simulação de pagamento).
     *
     * Validações:
     * - Pedido existe e pertence ao usuário
     * - Status atual é CRIADO
     *
     * @param email email do usuário
     * @param pedidoId ID do pedido
     * @return pedido atualizado
     */
    @Transactional
    public PedidoDTO pagarPedido(String email, UUID pedidoId) {
        Usuario usuario = buscarUsuarioPorEmail(email);

        Pedido pedido = pedidoRepository.findByIdAndUsuario(pedidoId, usuario)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        // Valida e atualiza status
        pedido.marcarComoPago();

        pedido = pedidoRepository.save(pedido);

        log.info("Pedido pago: ID={}, Usuario={}", pedidoId, email);

        return pedidoMapper.toDTO(pedido);
    }

    /**
     * Cancela um pedido.
     * Apenas pedidos com status CRIADO ou PAGO podem ser cancelados.
     *
     * @param email email do usuário
     * @param pedidoId ID do pedido
     * @return pedido cancelado
     */
    @Transactional
    public PedidoDTO cancelarPedido(String email, UUID pedidoId) {
        Usuario usuario = buscarUsuarioPorEmail(email);

        Pedido pedido = pedidoRepository.findByIdAndUsuario(pedidoId, usuario)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        // Valida e cancela
        pedido.cancelar();

        pedido = pedidoRepository.save(pedido);

        log.info("Pedido cancelado: ID={}, Usuario={}", pedidoId, email);

        return pedidoMapper.toDTO(pedido);
    }

    // ========== MÉTODOS ADMINISTRATIVOS ==========

    /**
     * Marca pedido como enviado (apenas ADMIN).
     *
     * @param pedidoId ID do pedido
     * @return pedido atualizado
     */
    @Transactional
    public PedidoDTO marcarComoEnviado(UUID pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.marcarComoEnviado();

        pedido = pedidoRepository.save(pedido);

        log.info("Pedido enviado: ID={}", pedidoId);

        return pedidoMapper.toDTO(pedido);
    }

    /**
     * Marca pedido como entregue (apenas ADMIN).
     *
     * @param pedidoId ID do pedido
     * @return pedido atualizado
     */
    @Transactional
    public PedidoDTO marcarComoEntregue(UUID pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.marcarComoEntregue();

        pedido = pedidoRepository.save(pedido);

        log.info("Pedido entregue: ID={}", pedidoId);

        return pedidoMapper.toDTO(pedido);
    }

    /**
     * Lista todos os pedidos por status (apenas ADMIN).
     *
     * @param status status do pedido
     * @return lista de pedidos
     */
    @Transactional(readOnly = true)
    public List<PedidoDTO> listarPedidosPorStatus(StatusPedido status) {
        List<Pedido> pedidos = pedidoRepository.findByStatus(status);

        return pedidos.stream()
                .map(pedidoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca qualquer pedido por ID (apenas ADMIN).
     *
     * @param pedidoId ID do pedido
     * @return pedido detalhado
     */
    @Transactional(readOnly = true)
    public PedidoDetalhadoDTO buscarPedidoAdmin(UUID pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        return pedidoMapper.toDetalhadoDTO(pedido);
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Busca usuário por email.
     */
    private Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    /**
     * Valida estoque de todos os produtos do carrinho.
     * Lança exceção se algum produto não tiver estoque suficiente.
     */
    private void validarEstoqueProdutos(Carrinho carrinho) {
        for (ItemCarrinho item : carrinho.getItens()) {
            Produto produto = item.getProduto();
            Integer quantidadeNecessaria = item.getQuantidade();

            // Valida produto ativo
            if (!produto.getAtivo()) {
                throw new RuntimeException(
                        String.format("Produto '%s' não está mais disponível", produto.getNome())
                );
            }

            // Valida estoque suficiente
            if (!produto.temEstoqueSuficiente(quantidadeNecessaria)) {
                throw new RuntimeException(
                        String.format(
                                "Estoque insuficiente para o produto '%s'. Disponível: %d, Solicitado: %d",
                                produto.getNome(),
                                produto.getEstoque().getQuantidade(),
                                quantidadeNecessaria
                        )
                );
            }
        }
    }
}
