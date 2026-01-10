package br.com.minhavenda.minhavenda.application.dto.pedido;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    @NotBlank(message = "Endereço de entrega é obrigatório")
    @Size(max = 500, message = "Endereço deve ter no máximo 500 caracteres")
    private String enderecoEntrega;

    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    private String observacoes;
}
