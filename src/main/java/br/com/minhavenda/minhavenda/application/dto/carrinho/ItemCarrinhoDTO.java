package br.com.minhavenda.minhavenda.application.dto.carrinho;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para representar um item do carrinho.
 * 
 * Contém informações do produto, quantidade e valores.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCarrinhoDTO {
    
    private UUID id;
    
    private UUID produtoId;
    
    private String produtoNome;
    
    private String produtoDescricao;
    
    private Integer quantidade;
    
    private BigDecimal precoUnitario;
    
    private BigDecimal subtotal;
}
