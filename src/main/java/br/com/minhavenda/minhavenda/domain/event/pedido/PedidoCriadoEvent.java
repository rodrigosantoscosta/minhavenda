package br.com.minhavenda.minhavenda.domain.event.pedido;


import lombok.Getter;

import java.util.UUID;

/**
 * Evento disparado quando um pedido é criado
 */
@Getter
public class PedidoCriadoEvent extends BaseDomainEvent {
    private final UUID pedidoId;
    private final UUID usuarioId;
    private final String emailUsuario;
    private final String nomeUsuario;
    private final Double valorTotal;
    private final Integer quantidadeItens;

    public PedidoCriadoEvent(UUID pedidoId, UUID usuarioId, String emailUsuario,
                             String nomeUsuario, Double valorTotal, Integer quantidadeItens) {
        super();
        this.pedidoId = pedidoId;
        this.usuarioId = usuarioId;
        this.emailUsuario = emailUsuario;
        this.nomeUsuario = nomeUsuario;
        this.valorTotal = valorTotal;
        this.quantidadeItens = quantidadeItens;
    }
}
