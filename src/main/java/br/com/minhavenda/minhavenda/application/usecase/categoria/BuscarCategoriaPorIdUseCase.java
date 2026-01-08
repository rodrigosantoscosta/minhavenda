package br.com.minhavenda.minhavenda.application.usecase.categoria;

import br.com.minhavenda.minhavenda.application.dto.CategoriaDTO;
import br.com.minhavenda.minhavenda.application.mapper.CategoriaMapper;
import br.com.minhavenda.minhavenda.domain.entity.Categoria;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Use Case para buscar categoria por ID.
 */
@Service
@RequiredArgsConstructor
public class BuscarCategoriaPorIdUseCase {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    @Transactional(readOnly = true)
    public Optional<CategoriaDTO> executar(Long id) {
        Optional<Categoria> categoria = categoriaRepository.findById(id);
        return categoria.map(categoriaMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public CategoriaDTO executarOuFalhar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + id));
        return categoriaMapper.toDTO(categoria);
    }
}