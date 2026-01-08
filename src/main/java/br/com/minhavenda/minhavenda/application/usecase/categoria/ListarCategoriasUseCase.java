package br.com.minhavenda.minhavenda.application.usecase.categoria;

import br.com.minhavenda.minhavenda.application.dto.CategoriaDTO;
import br.com.minhavenda.minhavenda.application.mapper.CategoriaMapper;
import br.com.minhavenda.minhavenda.domain.entity.Categoria;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use Case para listar categorias.
 */
@Service
@RequiredArgsConstructor
public class ListarCategoriasUseCase {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    @Transactional(readOnly = true)
    public List<CategoriaDTO> executar() {
        List<Categoria> categorias = categoriaRepository.findByAtivoTrue();
        return categoriaMapper.toDTO(categorias);
    }

    @Transactional(readOnly = true)
    public Page<CategoriaDTO> executar(Pageable pageable) {
        Page<Categoria> categorias = categoriaRepository.findByAtivoTrue(pageable);
        return categorias.map(categoriaMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarTodas() {
        List<Categoria> categorias = categoriaRepository.findAll();
        return categoriaMapper.toDTO(categorias);
    }

    @Transactional(readOnly = true)
    public Page<CategoriaDTO> listarTodas(Pageable pageable) {
        Page<Categoria> categorias = categoriaRepository.findAll(pageable);
        return categorias.map(categoriaMapper::toDTO);
    }
}