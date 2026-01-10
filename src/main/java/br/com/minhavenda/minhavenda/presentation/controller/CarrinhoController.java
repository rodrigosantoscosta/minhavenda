package br.com.minhavenda.minhavenda.presentation.controller;

import br.com.minhavenda.minhavenda.application.dto.carrinho.AdicionarItemCarrinhoRequest;
import br.com.minhavenda.minhavenda.application.dto.carrinho.AtualizarItemCarrinhoRequest;
import br.com.minhavenda.minhavenda.application.dto.carrinho.CarrinhoDTO;
import br.com.minhavenda.minhavenda.application.service.CarrinhoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller para gerenciar carrinho de compras.
 * 
 * Endpoints:
 * - GET /carrinho - Buscar carrinho do usuário logado
 * - POST /carrinho/itens - Adicionar produto ao carrinho
 * - PUT /carrinho/itens/{id} - Atualizar quantidade do item
 * - DELETE /carrinho/itens/{id} - Remover item do carrinho
 * - DELETE /carrinho - Limpar carrinho
 * 
 * Todos os endpoints exigem autenticação.
 */
@RestController
@RequestMapping("/carrinho")
@RequiredArgsConstructor
@Tag(name = "Carrinho", description = "Gerenciamento do carrinho de compras")
@SecurityRequirement(name = "bearer-auth")
public class CarrinhoController {

    private final CarrinhoService carrinhoService;

    /**
     * Busca carrinho do usuário logado.
     * 
     * GET /api/carrinho
     * 
     * Se usuário não tem carrinho, retorna carrinho vazio.
     * 
     * Response (200):
     * {
     *   "id": "uuid",
     *   "usuarioId": "uuid",
     *   "status": "ATIVO",
     *   "itens": [
     *     {
     *       "id": "uuid",
     *       "produtoId": "uuid",
     *       "produtoNome": "Notebook",
     *       "quantidade": 2,
     *       "precoUnitario": 3999.99,
     *       "subtotal": 7999.98
     *     }
     *   ],
     *   "valorTotal": 7999.98,
     *   "quantidadeTotal": 2
     * }
     * 
     * @param authentication dados do usuário logado
     * @return carrinho do usuário
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Buscar carrinho",
        description = "Retorna o carrinho ativo do usuário logado"
    )
    public ResponseEntity<CarrinhoDTO> buscarCarrinho(Authentication authentication) {
        String email = authentication.getName();
        CarrinhoDTO carrinho = carrinhoService.buscarCarrinho(email);
        return ResponseEntity.ok(carrinho);
    }

    /**
     * Adiciona produto ao carrinho.
     * 
     * POST /api/carrinho/itens
     * 
     * Se produto já existe no carrinho, incrementa quantidade.
     * 
     * Request:
     * {
     *   "produtoId": "uuid",
     *   "quantidade": 2
     * }
     * 
     * Response (201):
     * {
     *   "id": "uuid",
     *   "itens": [...],
     *   "valorTotal": 7999.98
     * }
     * 
     * @param authentication dados do usuário logado
     * @param request produto e quantidade
     * @return carrinho atualizado
     */
    @PostMapping("/itens")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Adicionar item ao carrinho",
        description = "Adiciona um produto ao carrinho ou incrementa quantidade se já existir"
    )
    public ResponseEntity<CarrinhoDTO> adicionarItem(
            Authentication authentication,
            @Valid @RequestBody AdicionarItemCarrinhoRequest request
    ) {
        String email = authentication.getName();
        CarrinhoDTO carrinho = carrinhoService.adicionarItem(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(carrinho);
    }

    /**
     * Atualiza quantidade de um item no carrinho.
     * 
     * PUT /api/carrinho/itens/{itemId}
     * 
     * Request:
     * {
     *   "quantidade": 5
     * }
     * 
     * Response (200):
     * {
     *   "id": "uuid",
     *   "itens": [...],
     *   "valorTotal": 19999.95
     * }
     * 
     * @param authentication dados do usuário logado
     * @param itemId ID do item
     * @param request nova quantidade
     * @return carrinho atualizado
     */
    @PutMapping("/itens/{itemId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Atualizar quantidade do item",
        description = "Atualiza a quantidade de um item específico no carrinho"
    )
    public ResponseEntity<CarrinhoDTO> atualizarItem(
            Authentication authentication,
            @PathVariable UUID itemId,
            @Valid @RequestBody AtualizarItemCarrinhoRequest request
    ) {
        String email = authentication.getName();
        CarrinhoDTO carrinho = carrinhoService.atualizarItem(email, itemId, request);
        return ResponseEntity.ok(carrinho);
    }

    /**
     * Remove item do carrinho.
     * 
     * DELETE /api/carrinho/itens/{itemId}
     * 
     * Response (200):
     * {
     *   "id": "uuid",
     *   "itens": [...],
     *   "valorTotal": 3999.99
     * }
     * 
     * @param authentication dados do usuário logado
     * @param itemId ID do item
     * @return carrinho atualizado
     */
    @DeleteMapping("/itens/{itemId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Remover item do carrinho",
        description = "Remove um item específico do carrinho"
    )
    public ResponseEntity<CarrinhoDTO> removerItem(
            Authentication authentication,
            @PathVariable UUID itemId
    ) {
        String email = authentication.getName();
        CarrinhoDTO carrinho = carrinhoService.removerItem(email, itemId);
        return ResponseEntity.ok(carrinho);
    }

    /**
     * Limpa todos os itens do carrinho.
     * 
     * DELETE /api/carrinho
     * 
     * Response (200):
     * {
     *   "id": "uuid",
     *   "itens": [],
     *   "valorTotal": 0.00,
     *   "quantidadeTotal": 0
     * }
     * 
     * @param authentication dados do usuário logado
     * @return carrinho vazio
     */
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Limpar carrinho",
        description = "Remove todos os itens do carrinho"
    )
    public ResponseEntity<CarrinhoDTO> limparCarrinho(Authentication authentication) {
        String email = authentication.getName();
        CarrinhoDTO carrinho = carrinhoService.limparCarrinho(email);
        return ResponseEntity.ok(carrinho);
    }
}
