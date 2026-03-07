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
public class EnviarPedidoUseCase {
    
    private final PedidoRepository pedidoRepository;
    private final DomainEventPublisher eventPublisher;
    
    @Transactional
    public Pedido executar(UUID pedidoId, String codigoRastreio, 
                          String transportadora, String telefoneUsuario) {
        log.info("Enviando pedido: {}", pedidoId);
        
        // 1. Buscar pedido
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        
        // 2. Enviar (isso registra PedidoEnviadoEvent internamente!)
        pedido.enviar(codigoRastreio, transportadora, telefoneUsuario);
        
        // 3. Salvar
        Pedido pedidoEnviado = pedidoRepository.save(pedido);
        
        log.info("Pedido enviado: {} - Rastreio: {}", pedidoId, codigoRastreio);
        
        // 4.  PUBLICAR EVENTOS
        eventPublisher.publishAll(pedidoEnviado.getDomainEvents());
        pedidoEnviado.limparEventos();
        
        return pedidoEnviado;
    }
}
