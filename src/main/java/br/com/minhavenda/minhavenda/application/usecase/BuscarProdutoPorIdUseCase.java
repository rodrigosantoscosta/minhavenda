package br.com.minhavenda.minhavenda.application.usecase;

import br.com.minhavenda.minhavenda.application.dto.ProdutoDTO;
import br.com.minhavenda.minhavenda.application.mapper.ProdutoMapper;
import br.com.minhavenda.minhavenda.domain.entity.Produto;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Use Case para buscar produto por ID.
 * 
 * Responsabilidades:
 * - Buscar produto no repository
 * - Validar existência
 * - Converter para DTO
 */
@Service
@RequiredArgsConstructor
public class BuscarProdutoPorIdUseCase {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    /**
     * Busca produto por ID.
     * 
     * @param id ID do produto
     * @return Optional com ProdutoDTO se encontrado
     */
    @Transactional(readOnly = true)
    public Optional<ProdutoDTO> executar(UUID id) {
        Optional<Produto> produto = produtoRepository.findById(id);
        return produto.map(produtoMapper::toDTO);
    }

    /**
     * Busca produto por ID e lança exceção se não encontrado.
     * 
     * @param id ID do produto
     * @return ProdutoDTO
     * @throws RuntimeException se produto não encontrado
     */
    @Transactional(readOnly = true)
    public ProdutoDTO executarOuFalhar(UUID id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
        return produtoMapper.toDTO(produto);
    }
}
