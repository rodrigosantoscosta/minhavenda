package br.com.minhavenda.minhavenda.application.usecase.categoria;

import br.com.minhavenda.minhavenda.application.dto.CategoriaDTO;
import br.com.minhavenda.minhavenda.application.mapper.CategoriaMapper;
import br.com.minhavenda.minhavenda.domain.entity.Categoria;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para criar nova categoria.
 */
@Service
@RequiredArgsConstructor
public class CriarCategoriaUseCase {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    @Transactional
    public CategoriaDTO executar(CategoriaDTO categoriaDTO) {
        // Validar se nome já existe
        if (categoriaRepository.existsByNomeIgnoreCase(categoriaDTO.getNome())) {
            throw new RuntimeException("Já existe uma categoria com o nome: " + categoriaDTO.getNome());
        }

        // Criar categoria
        Categoria categoria = Categoria.builder()
                .nome(categoriaDTO.getNome())
                .descricao(categoriaDTO.getDescricao())
                .ativo(categoriaDTO.getAtivo() != null ? categoriaDTO.getAtivo() : true)
                .build();

        // Salvar
        Categoria categoriaSalva = categoriaRepository.save(categoria);

        // Retornar DTO
        return categoriaMapper.toDTO(categoriaSalva);
    }
}