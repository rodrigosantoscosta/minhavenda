package br.com.minhavenda.minhavenda.presentation.controller;

import br.com.minhavenda.minhavenda.application.dto.ProdutoDTO;
import br.com.minhavenda.minhavenda.application.usecase.produto.BuscarProdutoPorIdUseCase;
import br.com.minhavenda.minhavenda.application.usecase.produto.CriarProdutoUseCase;
import br.com.minhavenda.minhavenda.application.usecase.produto.ListarProdutosUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST para operações com Produtos.
 *
 * Níveis de acesso:
 * - GET (listar/buscar): PÚBLICO - Qualquer pessoa
 * - POST (criar): ADMIN apenas
 * - PUT (atualizar): ADMIN apenas
 * - DELETE (excluir): ADMIN apenas
 */
@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Endpoints para gerenciamento de produtos")
public class ProdutoController {

    private final ListarProdutosUseCase listarProdutosUseCase;
    private final BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;
    private final CriarProdutoUseCase criarProdutoUseCase;


    /**
     * Lista todos os produtos ativos (sem paginação).
     * GET /produtos
     *
     * PÚBLICO - Não precisa autenticação
     */
    @GetMapping
    @Operation(summary = "Listar produtos ativos", description = "Retorna lista de todos os produtos ativos")
    public ResponseEntity<List<ProdutoDTO>> listarProdutos() {
        List<ProdutoDTO> produtos = listarProdutosUseCase.executar();
        return ResponseEntity.ok(produtos);
    }

    /**
     * Lista produtos ativos com paginação.
     * GET /produtos/paginado?page=0&size=20&sort=nome,asc
     *
     * PÚBLICO - Não precisa autenticação
     */
    @GetMapping("/paginado")
    @Operation(summary = "Listar produtos com paginação", description = "Retorna produtos ativos paginados")
    public ResponseEntity<Page<ProdutoDTO>> listarProdutosPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<ProdutoDTO> produtos = listarProdutosUseCase.executar(pageable);

        return ResponseEntity.ok(produtos);
    }

    /**
     * Busca produto por ID.
     * GET /produtos/{id}
     *
     * PÚBLICO - Não precisa autenticação
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID", description = "Retorna um produto específico")
    public ResponseEntity<ProdutoDTO> buscarPorId(@PathVariable UUID id) {
        return buscarProdutoPorIdUseCase.executar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lista todos os produtos (incluindo inativos).
     * GET /produtos/todos
     *
     * PROTEGIDO - Apenas ADMIN
     */
    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos produtos", description = "Retorna todos os produtos (ativos e inativos)")
    public ResponseEntity<List<ProdutoDTO>> listarTodos() {
        List<ProdutoDTO> produtos = listarProdutosUseCase.listarTodos();
        return ResponseEntity.ok(produtos);
    }

    /**
     * Lista todos os produtos com paginação (incluindo inativos).
     * GET /produtos/todos/paginado?page=0&size=20
     *
     * PROTEGIDO - Apenas ADMIN
     */
    @GetMapping("/todos/paginado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos produtos com paginação", description = "Retorna todos os produtos paginados")
    public ResponseEntity<Page<ProdutoDTO>> listarTodosPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nome") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<ProdutoDTO> produtos = listarProdutosUseCase.listarTodos(pageable);

        return ResponseEntity.ok(produtos);
    }

    /**
     * Cria um novo produto.
     * POST /produtos
     *
     * PROTEGIDO - Apenas ADMIN pode criar produtos
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar novo produto",
            description = "Cria um novo produto com os dados fornecidos. Apenas ADMIN.")
    public ResponseEntity<ProdutoDTO> criarProduto(@Valid @RequestBody ProdutoDTO produtoDTO) {
        ProdutoDTO produtoCriado = criarProdutoUseCase.execute(produtoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoCriado);
    }
}