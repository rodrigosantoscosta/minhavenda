package br.com.minhavenda.minhavenda.application.dto.carrinho;

import br.com.minhavenda.minhavenda.domain.enums.StatusCarrinho;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO para representar o carrinho completo.
 * 
 * Contém todos os itens e valores totais.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarrinhoDTO {
    
    private UUID id;
    
    private UUID usuarioId;
    
    private StatusCarrinho status;
    
    @Builder.Default
    private List<ItemCarrinhoDTO> itens = new ArrayList<>();
    
    private BigDecimal valorTotal;
    
    private Integer quantidadeTotal;
    
    private LocalDateTime dataCriacao;
    
    private LocalDateTime dataAtualizacao;
}
