package br.com.minhavenda.minhavenda.application.usecase.pedido;

import br.com.minhavenda.minhavenda.application.event.DomainEventPublisher;
import br.com.minhavenda.minhavenda.domain.entity.Pedido;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagarPedidoUseCase {
    
    private final PedidoRepository pedidoRepository;
    private final DomainEventPublisher eventPublisher;
    
    @Transactional
    public Pedido executar(UUID pedidoId, String metodoPagamento) {
        log.info("Processando pagamento do pedido: {}", pedidoId);
        
        // 1. Buscar pedido
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        
        // 2. Pagar (isso registra PedidoPagoEvent internamente!)
        pedido.pagar(metodoPagamento);
        
        // 3. Salvar
        Pedido pedidoPago = pedidoRepository.save(pedido);
        
        log.info("Pedido pago: {} - Método: {}", pedidoId, metodoPagamento);
        
        // 4. ✅ PUBLICAR EVENTOS
        eventPublisher.publishAll(pedidoPago.getDomainEvents());
        pedidoPago.limparEventos();
        
        return pedidoPago;
    }
}