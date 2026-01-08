package br.com.minhavenda.minhavenda.application.usecase.produto;

import br.com.minhavenda.minhavenda.application.dto.ProdutoDTO;
import br.com.minhavenda.minhavenda.application.mapper.ProdutoMapper;
import br.com.minhavenda.minhavenda.domain.entity.Produto;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use Case para listar produtos.
 * 
 * Responsabilidades:
 * - Coordenar busca de produtos
 * - Aplicar filtros
 * - Converter para DTO
 */
@Service
@RequiredArgsConstructor
public class ListarProdutosUseCase {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    /**
     * Lista todos os produtos ativos.
     */
    @Transactional(readOnly = true)
    public List<ProdutoDTO> executar() {
        List<Produto> produtos = produtoRepository.findByAtivo(true);
        return produtoMapper.toDTO(produtos);
    }

    /**
     * Lista produtos ativos com paginação.
     */
    @Transactional(readOnly = true)
    public Page<ProdutoDTO> executar(Pageable pageable) {
        Page<Produto> produtos = produtoRepository.findByAtivo(true, pageable);
        return produtos.map(produtoMapper::toDTO);
    }

    /**
     * Lista todos os produtos (ativos e inativos).
     */
    @Transactional(readOnly = true)
    public List<ProdutoDTO> listarTodos() {
        List<Produto> produtos = produtoRepository.findAll();
        return produtoMapper.toDTO(produtos);
    }

    /**
     * Lista todos os produtos com paginação.
     */
    @Transactional(readOnly = true)
    public Page<ProdutoDTO> listarTodos(Pageable pageable) {
        Page<Produto> produtos = produtoRepository.findAll(pageable);
        return produtos.map(produtoMapper::toDTO);
    }
}
