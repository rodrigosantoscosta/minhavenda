package br.com.minhavenda.minhavenda.application.dto;

import br.com.minhavenda.minhavenda.domain.valueobject.Money;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoDetalheDTO {
    private UUID id;
    private String nome;
    private String descricao;
    private Money preco;
    private Boolean ativo;
    private Instant dataCadastro;
    private CategoriaDTO categoria;
    private Integer quantidadeEstoque;
    private Double avaliacaoMedia;
    private Integer totalAvaliacoes;
}
