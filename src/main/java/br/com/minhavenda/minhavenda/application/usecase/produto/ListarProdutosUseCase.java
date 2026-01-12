package br.com.minhavenda.minhavenda.application.usecase.produto;

import br.com.minhavenda.minhavenda.application.dto.ProdutoDTO;
import br.com.minhavenda.minhavenda.application.dto.produto.FiltroProdutoRequest;
import br.com.minhavenda.minhavenda.application.mapper.ProdutoMapper;
import br.com.minhavenda.minhavenda.domain.entity.Produto;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.ProdutoRepository;
import br.com.minhavenda.minhavenda.infrastructure.persistence.specification.ProdutoSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Use Case para listar e buscar produtos.
 *
 * Responsabilidades:
 * - Listar produtos ativos (com/sem paginação)
 * - Buscar produtos por termo
 * - Buscar produtos com filtros dinâmicos
 * - Listar todos produtos (admin)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ListarProdutosUseCase {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    /**
     * Lista todos produtos ativos (sem paginação).
     * Uso: Listagem simples, catálogo pequeno.
     */
    public List<ProdutoDTO> executar() {
        log.debug("Listando todos produtos ativos");
        List<Produto> produtos = produtoRepository.findByAtivoTrue();

        return produtos.stream()
                .map(produtoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista produtos ativos com paginação.
     * Uso: Catálogo grande, navegação por páginas.
     *
     * @param pageable Configuração de paginação e ordenação
     */
    public Page<ProdutoDTO> executar(Pageable pageable) {
        log.debug("Listando produtos ativos com paginação: {}", pageable);
        Page<Produto> produtos = produtoRepository.findByAtivoTrue(pageable);

        return produtos.map(produtoMapper::toDTO);
    }

    /**
     * Busca produtos por termo no nome ou descrição.
     * Apenas produtos ativos.
     *
     * @param termo Termo de busca
     * @param pageable Configuração de paginação e ordenação
     */
    public Page<ProdutoDTO> buscarPorTermo(String termo, Pageable pageable) {
        log.debug("Buscando produtos por termo: '{}' com paginação: {}", termo, pageable);

        if (termo == null || termo.trim().isEmpty()) {
            return executar(pageable);
        }

        Page<Produto> produtos = produtoRepository.buscarPorTermo(termo.trim(), pageable);
        return produtos.map(produtoMapper::toDTO);
    }

    /**
     * Busca produtos com filtros dinâmicos.
     * Usa JPA Specification para construir query segura.
     *
     * @param filtro Filtros de busca
     * @param pageable Configuração de paginação e ordenação
     */
    public Page<ProdutoDTO> buscarComFiltros(FiltroProdutoRequest filtro, Pageable pageable) {
        log.debug("Buscando produtos com filtros: {} e paginação: {}", filtro, pageable);

        // Validar faixa de preço
        if (!filtro.isPrecoRangeValido()) {
            throw new IllegalArgumentException("Preço mínimo deve ser menor ou igual ao preço máximo");
        }

        // Se nenhum filtro aplicado, retorna apenas produtos ativos
        if (!filtro.hasAnyFilter() && Boolean.TRUE.equals(filtro.getAtivo())) {
            return executar(pageable);
        }

        // Construir Specification com filtros
        Specification<Produto> spec = ProdutoSpecification.comFiltros(filtro);

        // Executar query
        Page<Produto> produtos = produtoRepository.findAll(spec, pageable);

        log.debug("Encontrados {} produtos com os filtros aplicados", produtos.getTotalElements());

        return produtos.map(produtoMapper::toDTO);
    }

    /**
     * Lista todos produtos incluindo inativos (sem paginação).
     * ADMIN APENAS.
     */
    public List<ProdutoDTO> listarTodos() {
        log.debug("Listando TODOS produtos (ativos e inativos)");
        List<Produto> produtos = produtoRepository.findAll();

        return produtos.stream()
                .map(produtoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista todos produtos incluindo inativos (com paginação).
     * ADMIN APENAS.
     *
     * @param pageable Configuração de paginação e ordenação
     */
    public Page<ProdutoDTO> listarTodos(Pageable pageable) {
        log.debug("Listando TODOS produtos com paginação: {}", pageable);
        Page<Produto> produtos = produtoRepository.findAll(pageable);

        return produtos.map(produtoMapper::toDTO);
    }
}