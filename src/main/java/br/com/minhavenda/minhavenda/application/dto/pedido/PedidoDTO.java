package br.com.minhavenda.minhavenda.application.dto.pedido;

import br.com.minhavenda.minhavenda.domain.enums.StatusPedido;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {
    private UUID id;
    private StatusPedido status;
    private BigDecimal subtotal;
    private BigDecimal valorFrete;
    private BigDecimal valorDesconto;
    private BigDecimal valorTotal;
    private String enderecoEntrega;
    private String observacoes;
    private Integer quantidadeItens;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataPagamento;
    private LocalDateTime dataEnvio;
    private LocalDateTime dataEntrega;
}
