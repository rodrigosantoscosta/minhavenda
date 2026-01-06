package br.com.minhavenda.minhavenda.application.mapper;

import br.com.minhavenda.minhavenda.application.dto.ProdutoDTO;
import br.com.minhavenda.minhavenda.domain.entity.Produto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProdutoMapper {

    public ProdutoDTO toDTO(Produto produto) {
        if (produto == null) {
            return null;
        }

        return ProdutoDTO.builder()
                .id(produto.getId())
                .nome(produto.getNome())
                .descricao(produto.getDescricao())
                .preco(produto.getPreco())
                .ativo(produto.getAtivo())
                .dataCadastro(produto.getDataCadastro())
                .categoriaId(produto.getCategoria() != null ? produto.getCategoria().getId() : null)
                .categoriaNome(produto.getCategoria() != null ? produto.getCategoria().getNome() : null)
                .build();
    }

    public List<ProdutoDTO> toDTO(List<Produto> produtos) {
        return produtos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Produto toEntity(ProdutoDTO dto) {
        if (dto == null) {
            return null;
        }

        return Produto.builder()
                .id(dto.getId())
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .preco(dto.getPreco())
                .ativo(dto.getAtivo())
                .build();
    }
}
