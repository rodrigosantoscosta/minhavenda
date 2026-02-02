package br.com.minhavenda.minhavenda.infrastructure.notification;

import java.util.UUID;

/**
 * Service para envio de emails.
 */
public interface EmailService {

    /**
     * Envia email de confirmação de pedido criado.
     */
    void enviarEmailPedidoCriado(
            String destinatario,
            String nomeUsuario,
            UUID pedidoId,
            Double valorTotal,
            Integer quantidadeItens
    );

    /**
     * Envia email de confirmação de pagamento.
     */
    void enviarEmailPedidoPago(
            String destinatario,
            String nomeUsuario,
            UUID pedidoId,
            Double valorPago,
            String metodoPagamento
    );

    /**
     * Envia email com código de rastreio.
     */
    void enviarEmailPedidoEnviado(
            String destinatario,
            String nomeUsuario,
            UUID pedidoId,
            String codigoRastreio,
            String transportadora
    );

    /**
     * Envia email de pedido cancelado.
     */
    void enviarEmailPedidoCancelado(
            String destinatario,
            String nomeUsuario,
            UUID pedidoId,
            String motivo
    );
}