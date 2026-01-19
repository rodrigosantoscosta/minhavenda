package br.com.minhavenda.minhavenda.presentation.controller;


import br.com.minhavenda.minhavenda.application.usecase.estoque.AdicionarEstoqueRequest;
import br.com.minhavenda.minhavenda.application.usecase.estoque.AdicionarEstoqueUseCase;
import br.com.minhavenda.minhavenda.application.usecase.estoque.ConsultarEstoqueUseCase;
import br.com.minhavenda.minhavenda.application.usecase.estoque.RemoverEstoqueUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller para gerenciamento de estoque de produtos.
 * Segue arquitetura DDD usando Use Cases.
 */
@RestController
@RequestMapping("/estoque")
@RequiredArgsConstructor
public class EstoqueController {

    private final AdicionarEstoqueUseCase adicionarEstoqueUseCase;
    private final RemoverEstoqueUseCase removerEstoqueUseCase;
    private final ConsultarEstoqueUseCase consultarEstoqueUseCase;
//    private final AjustarEstoqueUseCase ajustarEstoqueUseCase;

    /**
     * Adicionar estoque de um produto
     * POST /estoque/produto/{produtoId}/adicionar
     */
    @PostMapping("/produto/{produtoId}/adicionar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensagemResponse> adicionarEstoque(
            @PathVariable UUID produtoId,
            @Valid @RequestBody AdicionarEstoqueRequest request
    ) {
        adicionarEstoqueUseCase.executar(produtoId, request.getQuantidade(), request.getMotivo());
        return ResponseEntity.ok(
                new MensagemResponse("Estoque adicionado com sucesso")
        );
    }

    /**
     * Remover estoque de um produto
     * POST /estoque/produto/{produtoId}/remover
     */
    @PostMapping("/produto/{produtoId}/remover")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensagemResponse> removerEstoque(
            @PathVariable UUID produtoId,
            @Valid @RequestBody AdicionarEstoqueRequest request
    ) {
        removerEstoqueUseCase.executar(produtoId, request.getQuantidade(), request.getMotivo());
        return ResponseEntity.ok(
                new MensagemResponse("Estoque removido com sucesso")
        );
    }

    /**
     * Consultar estoque de um produto
     * GET /estoque/produto/{produtoId}
     */
    @GetMapping("/produto/{produtoId}")
    public ResponseEntity<EstoqueResponse> consultarEstoque(@PathVariable UUID produtoId) {
        Integer quantidade = consultarEstoqueUseCase.executar(produtoId);
        return ResponseEntity.ok(
                new EstoqueResponse(produtoId, quantidade)
        );
    }

    /**
     * Ajustar estoque (definir quantidade exata)
     * PUT /estoque/produto/{produtoId}/ajustar
     */
//    @PutMapping("/produto/{produtoId}/ajustar")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<MensagemResponse> ajustarEstoque(
//            @PathVariable UUID produtoId,
//            @Valid @RequestBody AdicionarEstoqueRequest request
//    ) {
//        ajustarEstoqueUseCase.executar(produtoId, request.getQuantidade(), request.getMotivo());
//        return ResponseEntity.ok(
//                new MensagemResponse("Estoque ajustado com sucesso")
//        );
//    }

    // Records para respostas
    record MensagemResponse(String mensagem) {}
    record EstoqueResponse(UUID produtoId, Integer quantidade) {}
}