package br.com.minhavenda.minhavenda.presentation.controller;

import br.com.minhavenda.minhavenda.application.dto.ProdutoDTO;
import br.com.minhavenda.minhavenda.application.dto.produto.FiltroProdutoRequest;
import br.com.minhavenda.minhavenda.application.usecase.produto.BuscarProdutoPorIdUseCase;
import br.com.minhavenda.minhavenda.application.usecase.produto.CriarProdutoUseCase;
import br.com.minhavenda.minhavenda.application.usecase.produto.ListarProdutosUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST para operações com Produtos.
 *
 * Formato de ordenação: sort=campo:direção
 * Exemplos: sort=preco:asc, sort=preco:desc, sort=nome:asc
 */
@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Produtos", description = "Endpoints para gerenciamento de produtos")
public class ProdutoController {

    private final ListarProdutosUseCase listarProdutosUseCase;
    private final BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;
    private final CriarProdutoUseCase criarProdutoUseCase;

    @GetMapping
    @Operation(summary = "Listar produtos ativos")
    public ResponseEntity<List<ProdutoDTO>> listarProdutos() {
        log.info("GET /produtos");
        return ResponseEntity.ok(listarProdutosUseCase.executar());
    }

    @GetMapping("/paginado")
    @Operation(
            summary = "Listar produtos com paginação",
            description = "Ordenação: sort=campo:direção (ex: preco:asc, nome:desc)"
    )
    public ResponseEntity<Page<ProdutoDTO>> listarProdutosPaginado(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Ordenação: campo:direção (ex: preco:asc)")
            @RequestParam(defaultValue = "nome:asc") String sort
    ) {
        log.info("GET /produtos/paginado - page={}, size={}, sort={}", page, size, sort);
        Pageable pageable = createPageable(page, size, sort);
        return ResponseEntity.ok(listarProdutosUseCase.executar(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    public ResponseEntity<ProdutoDTO> buscarPorId(@PathVariable UUID id) {
        log.info("GET /produtos/{}", id);
        return buscarProdutoPorIdUseCase.executar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/busca")
    @Operation(
            summary = "Buscar produtos por termo",
            description = "Ordenação: sort=preco:asc ou sort=preco:desc "
    )
    public ResponseEntity<Page<ProdutoDTO>> buscarPorTermo(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Ordenação: campo:direção")
            @RequestParam(defaultValue = "nome:asc") String sort
    ) {
        log.info("GET /produtos/busca?q={}&sort={}", q, sort);
        Pageable pageable = createPageable(page, size, sort);
        return ResponseEntity.ok(listarProdutosUseCase.buscarPorTermo(q, pageable));
    }

    /**
     * Busca produtos com filtros.
     *
     * EXEMPLOS:
     *
     * 1. Ordenar por preço crescente:
     *    GET /produtos/buscar?sort=preco:asc
     *
     * 2. Ordenar por preço decrescente:
     *    GET /produtos/buscar?sort=preco:desc
     *
     * 3. Buscar notebooks do mais barato ao mais caro:
     *    GET /produtos/buscar?termo=notebook&sort=preco:asc
     *
     * 4. Buscar notebooks do mais caro ao mais barato:
     *    GET /produtos/buscar?termo=notebook&sort=preco:desc
     *
     * 5. Filtrar categoria e ordenar por preço:
     *    GET /produtos/buscar?categoriaId=1&sort=preco:desc
     *
     * 6. Busca completa:
     *    GET /produtos/buscar?termo=notebook&categoriaId=1&precoMin=2000&precoMax=4000&sort=preco:desc
     *
     * NOTA: URL encoded: sort=preco%3Adesc (onde %3A = :)
     */
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar produtos com filtros",
            description = """
            Formato: sort=campo:direção
 
            """
    )
    public ResponseEntity<Page<ProdutoDTO>> buscarComFiltros(
            @Parameter(description = "Termo de busca")
            @RequestParam(required = false) String termo,

            @Parameter(description = "ID da categoria")
            @RequestParam(required = false) Long categoriaId,

            @Parameter(description = "Preço mínimo")
            @RequestParam(required = false) @Min(0) BigDecimal precoMin,

            @Parameter(description = "Preço máximo")
            @RequestParam(required = false) @Min(0) BigDecimal precoMax,

            @Parameter(description = "Filtrar por ativo")
            @RequestParam(defaultValue = "true") Boolean ativo,

            @Parameter(description = "Ordenação: campo:direção (ex: preco:asc, preco:desc)")
            @RequestParam(defaultValue = "nome:asc") String sort,

            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        log.info("GET /produtos/buscar - termo={}, categoriaId={}, sort={}", termo, categoriaId, sort);

        Pageable pageable = createPageable(page, size, sort);

        FiltroProdutoRequest filtro = FiltroProdutoRequest.builder()
                .termo(termo)
                .categoriaId(categoriaId)
                .precoMin(precoMin)
                .precoMax(precoMax)
                .ativo(ativo)
                .build();

        Page<ProdutoDTO> produtos = listarProdutosUseCase.buscarComFiltros(filtro, pageable);
        log.info("Encontrados {} produtos", produtos.getTotalElements());

        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos produtos (ADMIN)")
    public ResponseEntity<List<ProdutoDTO>> listarTodos() {
        return ResponseEntity.ok(listarProdutosUseCase.listarTodos());
    }

    @GetMapping("/todos/paginado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos produtos paginado (ADMIN)")
    public ResponseEntity<Page<ProdutoDTO>> listarTodosPaginado(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "nome:asc") String sort
    ) {
        Pageable pageable = createPageable(page, size, sort);
        return ResponseEntity.ok(listarProdutosUseCase.listarTodos(pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar novo produto (ADMIN)")
    public ResponseEntity<ProdutoDTO> criarProduto(@Valid @RequestBody ProdutoDTO produtoDTO) {
        log.info("POST /produtos - {}", produtoDTO.getNome());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(criarProdutoUseCase.execute(produtoDTO));
    }

    // =========================================================================
    // MÉTODOS AUXILIARES
    // =========================================================================

    /**
     * Cria Pageable do formato 'campo:direção'.
     *
     * Formato: campo:direção
     * - Exemplos: preco:asc, nome:desc, dataCadastro:desc
     *
     * @param page Número da página
     * @param size Tamanho da página
     * @param sortParam Parâmetro de ordenação (formato: campo:direção)
     * @return Pageable configurado
     */
    private Pageable createPageable(int page, int size, String sortParam) {
        String[] parts = sortParam.split(":");
        String field = parts.length > 0 ? parts[0].trim() : "nome";
        String direction = parts.length > 1 ? parts[1].trim() : "asc";

        String validatedField = validateSortField(field);

        // Mapear "preco" para "preco.valor" (Money Value Object)
        if ("preco".equals(validatedField)) {
            validatedField = "preco.valor";
            log.debug("Mapeando 'preco' → 'preco.valor' (Money Value Object)");
        }

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        log.debug("Ordenação: {} {} (original: {})", validatedField, sortDirection, sortParam);

        return PageRequest.of(page, size, Sort.by(sortDirection, validatedField));
    }

    /**
     * Valida campo contra whitelist.
     */
    private String validateSortField(String field) {
        if (field == null || field.trim().isEmpty()) {
            return "nome";
        }

        List<String> allowedFields = List.of(
                "nome",
                "preco",
                "dataCadastro",
                "estoque",
                "categoria.nome"
        );

        if (!allowedFields.contains(field)) {
            log.warn("Campo '{}' não permitido. Usando 'nome'.", field);
            return "nome";
        }

        return field;
    }
}