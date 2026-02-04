package br.com.minhavenda.minhavenda.domain.event.pedido;


import lombok.Getter;

import java.util.UUID;

@Getter
public class PedidoEnviadoEvent extends BaseDomainEvent {
    private final UUID pedidoId;
    private final UUID usuarioId;
    private final String nomeUsuario;
    private final String emailUsuario;
    private final String telefone;
    private final String codigoRastreio;
    private final String transportadora;

    public PedidoEnviadoEvent(UUID pedidoId, UUID usuarioId, String nomeUsuario, String emailUsuario,
                              String telefone, String codigoRastreio, String transportadora) {
        super();
        this.pedidoId = pedidoId;
        this.usuarioId = usuarioId;
        this.nomeUsuario = nomeUsuario;
        this.emailUsuario = emailUsuario;
        this.telefone = telefone;
        this.codigoRastreio = codigoRastreio;
        this.transportadora = transportadora;
    }
}
