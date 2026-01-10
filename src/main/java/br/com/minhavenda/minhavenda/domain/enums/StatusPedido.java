package br.com.minhavenda.minhavenda.domain.enums;

/**
 * Status possíveis de um pedido.
 * 
 * Fluxo normal: CRIADO → PAGO → ENVIADO → ENTREGUE
 * 
 * Pode também ir para: CANCELADO (se cancelado antes de enviar)
 */
public enum StatusPedido {
    
    /**
     * Pedido criado mas ainda não pago.
     * Estado inicial após checkout.
     */
    CRIADO("Pedido criado"),
    
    /**
     * Pedido pago e confirmado.
     * Aguardando separação/envio.
     */
    PAGO("Pagamento confirmado"),
    
    /**
     * Pedido enviado para entrega.
     * Em trânsito para o cliente.
     */
    ENVIADO("Pedido enviado"),
    
    /**
     * Pedido entregue ao cliente.
     * Estado final de sucesso.
     */
    ENTREGUE("Pedido entregue"),
    
    /**
     * Pedido cancelado.
     * Pode ser pelo cliente ou admin.
     */
    CANCELADO("Pedido cancelado");
    
    private final String descricao;
    
    StatusPedido(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Verifica se o pedido pode ser cancelado.
     * Apenas pedidos CRIADO ou PAGO podem ser cancelados.
     */
    public boolean podeCancelar() {
        return this == CRIADO || this == PAGO;
    }
    
    /**
     * Verifica se o pedido pode ser pago.
     * Apenas pedidos CRIADO podem ser pagos.
     */
    public boolean podePagar() {
        return this == CRIADO;
    }
    
    /**
     * Verifica se o pedido pode ser enviado.
     * Apenas pedidos PAGO podem ser enviados.
     */
    public boolean podeEnviar() {
        return this == PAGO;
    }
    
    /**
     * Verifica se o pedido está finalizado.
     */
    public boolean isFinalizado() {
        return this == ENTREGUE || this == CANCELADO;
    }
}
