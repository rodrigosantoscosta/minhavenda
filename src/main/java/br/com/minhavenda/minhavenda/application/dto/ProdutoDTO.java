package br.com.minhavenda.minhavenda.application.dto;

import br.com.minhavenda.minhavenda.domain.valueobject.Money;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoDTO {
    private UUID id;
    private String nome;
    private String descricao;
    private Money preco;
    private Boolean ativo;
    private Instant dataCadastro;
    private Long categoriaId;
    private String categoriaNome;
}