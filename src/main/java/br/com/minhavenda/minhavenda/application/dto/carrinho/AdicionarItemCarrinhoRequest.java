package br.com.minhavenda.minhavenda.application.dto.carrinho;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request para adicionar item ao carrinho.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdicionarItemCarrinhoRequest {
    
    @NotNull(message = "ID do produto é obrigatório")
    private UUID produtoId;
    
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser no mínimo 1")
    private Integer quantidade;
}
