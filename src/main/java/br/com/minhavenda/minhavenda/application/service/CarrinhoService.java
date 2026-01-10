package br.com.minhavenda.minhavenda.application.service;

import br.com.minhavenda.minhavenda.application.dto.carrinho.AdicionarItemCarrinhoRequest;
import br.com.minhavenda.minhavenda.application.dto.carrinho.AtualizarItemCarrinhoRequest;
import br.com.minhavenda.minhavenda.application.dto.carrinho.CarrinhoDTO;
import br.com.minhavenda.minhavenda.application.dto.carrinho.ItemCarrinhoDTO;
import br.com.minhavenda.minhavenda.application.mapper.CarrinhoMapper;
import br.com.minhavenda.minhavenda.domain.entity.Carrinho;
import br.com.minhavenda.minhavenda.domain.entity.ItemCarrinho;
import br.com.minhavenda.minhavenda.domain.entity.Produto;
import br.com.minhavenda.minhavenda.domain.entity.Usuario;
import br.com.minhavenda.minhavenda.domain.enums.StatusCarrinho;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.CarrinhoRepository;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.ItemCarrinhoRepository;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.ProdutoRepository;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service para gerenciar carrinho de compras.
 * 
 * Responsabilidades:
 * - Buscar ou criar carrinho ativo do usuário
 * - Adicionar produtos ao carrinho
 * - Atualizar quantidade de itens
 * - Remover itens do carrinho
 * - Limpar carrinho
 * - Calcular valores totais
 */
@Service
@RequiredArgsConstructor
public class CarrinhoService {

    private final CarrinhoRepository carrinhoRepository;
    private final ItemCarrinhoRepository itemCarrinhoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarrinhoMapper carrinhoMapper;

    /**
     * Busca carrinho ativo do usuário.
     * Se não existir, cria um novo.
     * 
     * @param email email do usuário
     * @return carrinho do usuário
     */
    @Transactional(readOnly = true)
    public CarrinhoDTO buscarCarrinho(String email) {
        Usuario usuario = buscarUsuarioPorEmail(email);
        
        Carrinho carrinho = carrinhoRepository
                .findByUsuarioAndStatus(usuario, StatusCarrinho.ATIVO)
                .orElseGet(() -> criarNovoCarrinho(usuario));
        
        return carrinhoMapper.toDTO(carrinho);
    }

    /**
     * Adiciona produto ao carrinho.
     * 
     * Se produto já existe no carrinho, incrementa quantidade.
     * Se não existe, cria novo item.
     * 
     * @param email email do usuário
     * @param request dados do item a adicionar
     * @return carrinho atualizado
     */
    @Transactional
    public CarrinhoDTO adicionarItem(String email, AdicionarItemCarrinhoRequest request) {
        // 1. Buscar usuário
        Usuario usuario = buscarUsuarioPorEmail(email);
        
        // 2. Buscar ou criar carrinho ativo
        Carrinho carrinho = carrinhoRepository
                .findByUsuarioAndStatus(usuario, StatusCarrinho.ATIVO)
                .orElseGet(() -> criarNovoCarrinho(usuario));
        
        // 3. Buscar produto
        Produto produto = buscarProdutoPorId(request.getProdutoId());
        
        // 4. Validar produto ativo
        if (!produto.getAtivo()) {
            throw new RuntimeException("Produto não está disponível para compra");
        }

        Integer quantidadePedida = request.getQuantidade();
        // 5. Validar estoque
        if (!produto.temEstoqueSuficiente(quantidadePedida)) {
            throw new RuntimeException(
                    "Estoque insuficiente. Disponível: " + produto.getEstoque()
            );
        }
        
        // 6. Verificar se produto já está no carrinho
        ItemCarrinho itemExistente = itemCarrinhoRepository
                .findByCarrinhoAndProduto(carrinho, produto)
                .orElse(null);
        
        if (itemExistente != null) {
            // Produto já existe: incrementa quantidade
            Integer novaQuantidade = itemExistente.getQuantidade() + request.getQuantidade();
            
            // Valida estoque novamente
            if (!produto.temEstoqueSuficiente(novaQuantidade)) {
                throw new RuntimeException(
                        "Estoque insuficiente. Disponível: " + produto.getEstoque()
                );
            }
            
            itemExistente.setQuantidade(novaQuantidade);
            itemExistente.calcularSubtotal();
            itemCarrinhoRepository.save(itemExistente);
            
        } else {
            // Produto novo: cria item
            ItemCarrinho novoItem = ItemCarrinho.builder()
                    .carrinho(carrinho)
                    .produto(produto)
                    .quantidade(request.getQuantidade())
                    .precoUnitario(produto.getPreco().getValor())
                    .build();
            
            novoItem.calcularSubtotal();
            itemCarrinhoRepository.save(novoItem);
            carrinho.getItens().add(novoItem);
        }
        
        // 7. Recalcular total e salvar carrinho
        carrinho.calcularValorTotal();
        carrinhoRepository.save(carrinho);
        
        return carrinhoMapper.toDTO(carrinho);
    }

    /**
     * Atualiza quantidade de um item no carrinho.
     * 
     * @param email email do usuário
     * @param itemId ID do item
     * @param request nova quantidade
     * @return carrinho atualizado
     */
    @Transactional
    public CarrinhoDTO atualizarItem(
            String email,
            UUID itemId,
            AtualizarItemCarrinhoRequest request
    ) {
        Integer quantidadePedida = request.getQuantidade();

        // 1. Buscar usuário e carrinho
        Usuario usuario = buscarUsuarioPorEmail(email);
        Carrinho carrinho = buscarCarrinhoAtivoDoUsuario(usuario);
        
        // 2. Buscar item
        ItemCarrinho item = itemCarrinhoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado no carrinho"));
        
        // 3. Validar que item pertence ao carrinho do usuário
        if (!item.getCarrinho().getId().equals(carrinho.getId())) {
            throw new RuntimeException("Item não pertence ao seu carrinho");
        }


        // 4. Validar estoque
        Produto produto = item.getProduto();
        if (!produto.temEstoqueSuficiente(quantidadePedida)) {
            throw new RuntimeException(
                    "Estoque insuficiente. Disponível: " + produto.getEstoque()
            );
        }
        
        // 5. Atualizar quantidade e recalcular
        item.setQuantidade(request.getQuantidade());
        item.calcularSubtotal();
        itemCarrinhoRepository.save(item);
        
        // 6. Recalcular total do carrinho
        carrinho.calcularValorTotal();
        carrinhoRepository.save(carrinho);
        
        return carrinhoMapper.toDTO(carrinho);
    }

    /**
     * Remove item do carrinho.
     * 
     * @param email email do usuário
     * @param itemId ID do item
     * @return carrinho atualizado
     */
    @Transactional
    public CarrinhoDTO removerItem(String email, UUID itemId) {
        // 1. Buscar usuário e carrinho
        Usuario usuario = buscarUsuarioPorEmail(email);
        Carrinho carrinho = buscarCarrinhoAtivoDoUsuario(usuario);
        
        // 2. Buscar item
        ItemCarrinho item = itemCarrinhoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado no carrinho"));
        
        // 3. Validar que item pertence ao carrinho do usuário
        if (!item.getCarrinho().getId().equals(carrinho.getId())) {
            throw new RuntimeException("Item não pertence ao seu carrinho");
        }
        
        // 4. Remover item
        carrinho.getItens().remove(item);
        itemCarrinhoRepository.delete(item);
        
        // 5. Recalcular total do carrinho
        carrinho.calcularValorTotal();
        carrinhoRepository.save(carrinho);
        
        return carrinhoMapper.toDTO(carrinho);
    }

    /**
     * Limpa todos os itens do carrinho.
     * 
     * @param email email do usuário
     * @return carrinho vazio
     */
    @Transactional
    public CarrinhoDTO limparCarrinho(String email) {
        // 1. Buscar usuário e carrinho
        Usuario usuario = buscarUsuarioPorEmail(email);
        Carrinho carrinho = buscarCarrinhoAtivoDoUsuario(usuario);
        
        // 2. Remover todos os itens
        itemCarrinhoRepository.deleteAll(carrinho.getItens());
        carrinho.getItens().clear();
        
        // 3. Zerar total
        carrinho.calcularValorTotal();
        carrinhoRepository.save(carrinho);
        
        return carrinhoMapper.toDTO(carrinho);
    }

    // ========== MÉTODOS AUXILIARES ==========

    private Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    private Produto buscarProdutoPorId(UUID produtoId) {
        return produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    private Carrinho buscarCarrinhoAtivoDoUsuario(Usuario usuario) {
        return carrinhoRepository.findByUsuarioAndStatus(usuario, StatusCarrinho.ATIVO)
                .orElseThrow(() -> new RuntimeException("Carrinho não encontrado"));
    }

    private Carrinho criarNovoCarrinho(Usuario usuario) {
        Carrinho carrinho = Carrinho.builder()
                .usuario(usuario)
                .status(StatusCarrinho.ATIVO)
                .valorTotal(BigDecimal.ZERO)
                .build();
        
        return carrinhoRepository.save(carrinho);
    }
}
