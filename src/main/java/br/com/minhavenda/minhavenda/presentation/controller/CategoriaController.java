package br.com.minhavenda.minhavenda.presentation.controller;

import br.com.minhavenda.minhavenda.application.dto.AtualizarCategoriaRequest;
import br.com.minhavenda.minhavenda.application.dto.CategoriaDTO;
import br.com.minhavenda.minhavenda.application.usecase.categoria.AtualizarCategoriaUseCase;
import br.com.minhavenda.minhavenda.application.usecase.categoria.BuscarCategoriaPorIdUseCase;
import br.com.minhavenda.minhavenda.application.usecase.categoria.CriarCategoriaUseCase;
import br.com.minhavenda.minhavenda.application.usecase.categoria.ExcluirCategoriaUseCase;
import br.com.minhavenda.minhavenda.application.usecase.categoria.ListarCategoriasUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorias", description = "Endpoints para gerenciamento de categorias")
public class CategoriaController {

    private final ListarCategoriasUseCase listarCategoriasUseCase;
    private final BuscarCategoriaPorIdUseCase buscarCategoriaPorIdUseCase;
    private final CriarCategoriaUseCase criarCategoriaUseCase;
    private final AtualizarCategoriaUseCase atualizarCategoriaUseCase;
    private final ExcluirCategoriaUseCase excluirCategoriaUseCase;

    @GetMapping
    @Operation(summary = "Listar categorias ativas")
    public ResponseEntity<List<CategoriaDTO>> listarCategorias() {
        List<CategoriaDTO> categorias = listarCategoriasUseCase.executar();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/paginado")
    @Operation(summary = "Listar categorias com paginação")
    public ResponseEntity<Page<CategoriaDTO>> listarCategoriasPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<CategoriaDTO> categorias = listarCategoriasUseCase.executar(pageable);

        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID")
    public ResponseEntity<CategoriaDTO> buscarPorId(@PathVariable Long id) {
        return buscarCategoriaPorIdUseCase.executar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/todas")
    @Operation(summary = "Listar todas categorias")
    public ResponseEntity<List<CategoriaDTO>> listarTodas() {
        List<CategoriaDTO> categorias = listarCategoriasUseCase.listarTodas();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/todas/paginado")
    @Operation(summary = "Listar todas categorias com paginação")
    public ResponseEntity<Page<CategoriaDTO>> listarTodasPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nome") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<CategoriaDTO> categorias = listarCategoriasUseCase.listarTodas(pageable);

        return ResponseEntity.ok(categorias);
    }

    @PostMapping
    @Operation(summary = "Criar categoria")
    public ResponseEntity<CategoriaDTO> criarCategoria(@Valid @RequestBody CategoriaDTO categoriaDTO) {
        CategoriaDTO categoria = criarCategoriaUseCase.executar(categoriaDTO);

        URI location = URI.create("/categorias/" + categoria.getId());
        return ResponseEntity.created(location).body(categoria);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria")
    public ResponseEntity<CategoriaDTO> atualizarCategoria(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarCategoriaRequest request) {
        CategoriaDTO categoria = atualizarCategoriaUseCase.executar(id, request);
        return ResponseEntity.ok(categoria);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir categoria")
    public ResponseEntity<Void> excluirCategoria(@PathVariable Long id) {
        excluirCategoriaUseCase.executar(id);
        return ResponseEntity.noContent().build();
    }
}