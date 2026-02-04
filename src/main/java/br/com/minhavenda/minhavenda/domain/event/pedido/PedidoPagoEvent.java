package br.com.minhavenda.minhavenda.domain.event.pedido;


import lombok.Getter;
import java.util.UUID;

@Getter
public class PedidoPagoEvent extends BaseDomainEvent {
    private final UUID pedidoId;
    private final UUID usuarioId;
    private final String emailUsuario;
    private final Double valorPago;
    private final String metodoPagamento;

    public PedidoPagoEvent(UUID pedidoId, UUID usuarioId, String emailUsuario,
                           Double valorPago, String metodoPagamento) {
        super();
        this.pedidoId = pedidoId;
        this.usuarioId = usuarioId;
        this.emailUsuario = emailUsuario;
        this.valorPago = valorPago;
        this.metodoPagamento = metodoPagamento != null ? metodoPagamento : "Não informado";
    }
}
