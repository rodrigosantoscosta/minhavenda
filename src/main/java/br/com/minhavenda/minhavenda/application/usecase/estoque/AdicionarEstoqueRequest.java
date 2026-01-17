package br.com.minhavenda.minhavenda.application.usecase.estoque;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para adicionar estoque de um produto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdicionarEstoqueRequest {

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private Integer quantidade;

    private String motivo; // Opcional: "Reposição", "Devolução", etc.
}