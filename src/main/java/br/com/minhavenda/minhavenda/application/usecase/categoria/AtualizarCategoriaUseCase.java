package br.com.minhavenda.minhavenda.application.usecase.categoria;

import br.com.minhavenda.minhavenda.application.dto.AtualizarCategoriaRequest;
import br.com.minhavenda.minhavenda.application.dto.CategoriaDTO;
import br.com.minhavenda.minhavenda.application.mapper.CategoriaMapper;
import br.com.minhavenda.minhavenda.domain.entity.Categoria;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para atualizar categoria existente.
 */
@Service
@RequiredArgsConstructor
public class AtualizarCategoriaUseCase {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    @Transactional
    public CategoriaDTO executar(Long id, AtualizarCategoriaRequest request) {
        // Buscar categoria
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + id));

        // Validar nome duplicado (exceto própria categoria)
        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(request.getNome(), id)) {
            throw new RuntimeException("Já existe outra categoria com o nome: " + request.getNome());
        }

        // Atualizar dados
        categoria.setNome(request.getNome());
        categoria.setDescricao(request.getDescricao());

        if (request.getAtivo() != null) {
            categoria.setAtivo(request.getAtivo());
        }

        // Salvar
        Categoria categoriaAtualizada = categoriaRepository.save(categoria);

        // Retornar DTO
        return categoriaMapper.toDTO(categoriaAtualizada);
    }
}