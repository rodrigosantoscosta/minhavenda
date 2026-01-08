package br.com.minhavenda.minhavenda.application.mapper;

import br.com.minhavenda.minhavenda.application.dto.CategoriaDTO;
import br.com.minhavenda.minhavenda.domain.entity.Categoria;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoriaMapper {

    public CategoriaDTO toDTO(Categoria categoria) {
        if (categoria == null) {
            return null;
        }

        return CategoriaDTO.builder()
                .id(categoria.getId())
                .nome(categoria.getNome())
                .descricao(categoria.getDescricao())
                .ativo(categoria.getAtivo())
                .dataCadastro(categoria.getDataCadastro())
                .build();
    }

    public List<CategoriaDTO> toDTO(List<Categoria> categorias) {
        return categorias.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Categoria toEntity(CategoriaDTO dto) {
        if (dto == null) {
            return null;
        }

        return Categoria.builder()
                .id(dto.getId())
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .ativo(dto.getAtivo() != null ? dto.getAtivo() : true)
                .build();
    }
}