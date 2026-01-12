package br.com.minhavenda.minhavenda.application.usecase.categoria;

import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.CategoriaRepository;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para excluir categoria.
 */
@Service
@RequiredArgsConstructor
public class ExcluirCategoriaUseCase {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    @Transactional
    public void executar(Long id) {
        // Verificar se categoria existe
        if (!categoriaRepository.existsById(id)) {
            throw new RuntimeException("Categoria não encontrada com ID: " + id);
        }

        // Verificar se há produtos vinculados
        if (produtoRepository.countByCategoria(id) > 0) {
            throw new RuntimeException("Não é possível excluir categoria com produtos vinculados");
        }

        // Excluir
        categoriaRepository.deleteById(id);
    }
}