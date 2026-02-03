package br.com.minhavenda.minhavenda.infrastructure.notification;

import java.util.UUID;

public interface EmailService {

    /**
     * Envia email quando pedido é criado
     *
     * @param email email do usuário
     * @param nomeUsuario nome do usuário
     * @param pedidoId ID do pedido
     * @param valorTotal valor total em Double
     * @param quantidadeItens quantidade total de itens
     */
    void enviarEmailPedidoCriado(String email, String nomeUsuario, UUID pedidoId,
                                 Double valorTotal, Integer quantidadeItens);

    /**
     * Envia email quando pedido é pago
     *
     * @param email email do usuário
     * @param pedidoId ID do pedido
     * @param valorTotal valor total em Double
     * @param metodoPagamento método de pagamento utilizado
     */
    void enviarEmailPedidoPago(String email, UUID pedidoId,
                               Double valorTotal, String metodoPagamento);

    /**
     * Envia email quando pedido é enviado (com código de rastreio)
     *
     * @param email email do usuário
     * @param nomeUsuario nome do usuário
     * @param pedidoId ID do pedido
     * @param codigoRastreio código de rastreamento
     * @param transportadora nome da transportadora
     * @param telefone telefone do usuário (pode ser null)
     */
    void enviarEmailPedidoEnviado(String email, String nomeUsuario, UUID pedidoId,
                                  String codigoRastreio, String transportadora, String telefone);

    /**
     * Envia email quando pedido é cancelado
     *
     * @param email email do usuário
     * @param pedidoId ID do pedido
     * @param motivo motivo do cancelamento
     */
    void enviarEmailPedidoCancelado(String email, UUID pedidoId, String motivo);
}
