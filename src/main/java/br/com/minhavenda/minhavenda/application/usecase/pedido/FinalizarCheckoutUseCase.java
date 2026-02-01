package br.com.minhavenda.minhavenda.application.usecase.pedido;

import br.com.minhavenda.minhavenda.application.dto.pedido.CheckoutRequest;
import br.com.minhavenda.minhavenda.application.event.DomainEventPublisher;
import br.com.minhavenda.minhavenda.application.mapper.PedidoMapper;
import br.com.minhavenda.minhavenda.domain.entity.*;
import br.com.minhavenda.minhavenda.domain.enums.StatusCarrinho;
import br.com.minhavenda.minhavenda.domain.repository.*;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.CarrinhoRepository;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.PedidoRepository;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinalizarCheckoutUseCase {
    
    private final CarrinhoRepository carrinhoRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DomainEventPublisher eventPublisher; //  INJETAR
    private final PedidoMapper pedidoMapper;
    
    @Transactional
    public Pedido executar(UUID usuarioId, CheckoutRequest request) {
        log.info("Iniciando checkout para usuário: {}", usuarioId);
        
        // 1. Buscar usuário
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // 2. Buscar carrinho ativo
        Carrinho carrinho = carrinhoRepository
            .findByUsuarioIdAndStatus(usuarioId, StatusCarrinho.ATIVO)
            .orElseThrow(() -> new RuntimeException("Carrinho não encontrado"));
        
        // 3. Validar carrinho
        if (carrinho.getItens().isEmpty()) {
            throw new RuntimeException("Carrinho está vazio");
        }
        
        // 4. Calcular valores
        BigDecimal subtotal = carrinho.getValorTotal();
        BigDecimal valorFrete = BigDecimal.ZERO; // TODO: calcular frete
        BigDecimal valorDesconto = BigDecimal.ZERO; // TODO: aplicar cupom
        BigDecimal valorTotal = subtotal.add(valorFrete).subtract(valorDesconto);
        
        // 5. Criar pedido (isso já registra PedidoCriadoEvent internamente!)
        Pedido pedido = new Pedido(
            usuario,
            subtotal,
            valorFrete,
            valorDesconto,
            valorTotal,
            request.getEnderecoEntrega(),
            request.getObservacoes()
        );
        
        // 6. Copiar itens do carrinho para o pedido
        carrinho.getItens().forEach(itemCarrinho -> {
            pedido.adicionarItem(
                itemCarrinho.getProduto(),
                itemCarrinho.getQuantidade(),
                itemCarrinho.getPrecoUnitario()
            );
        });
        
        // 7. Finalizar carrinho (muda status para FINALIZADO)
        carrinho.finalizar();
        
        // 8. Salvar pedido e carrinho
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        carrinhoRepository.save(carrinho);
        
        log.info("Pedido criado: {} - Valor: R$ {}", 
                 pedidoSalvo.getId(), pedidoSalvo.getValorTotal());
        
        // 9. PUBLICAR EVENTOS DO AGGREGATE ROOT
        eventPublisher.publishAll(pedidoSalvo.getDomainEvents());
        pedidoSalvo.limparEventos();
        
        log.info(" Checkout finalizado com sucesso!");
        
        return pedidoSalvo;
    }
}