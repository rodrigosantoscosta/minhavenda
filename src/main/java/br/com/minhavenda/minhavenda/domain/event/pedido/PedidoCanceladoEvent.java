package br.com.minhavenda.minhavenda.domain.event.pedido;


import lombok.Getter;

import java.util.UUID;

@Getter
public class PedidoCanceladoEvent extends BaseDomainEvent {
    private final UUID pedidoId;
    private final UUID usuarioId;
    private final String emailUsuario;
    private final String motivoCancelamento;

    public PedidoCanceladoEvent(UUID pedidoId, UUID usuarioId,
                                String emailUsuario, String motivoCancelamento) {
        super();
        this.pedidoId = pedidoId;
        this.usuarioId = usuarioId;
        this.emailUsuario = emailUsuario;
        this.motivoCancelamento = motivoCancelamento;
    }
}


