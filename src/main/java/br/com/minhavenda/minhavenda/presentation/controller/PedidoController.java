package br.com.minhavenda.minhavenda.presentation.controller;

import br.com.minhavenda.minhavenda.application.dto.pedido.CheckoutRequest;
import br.com.minhavenda.minhavenda.application.dto.pedido.PedidoDTO;
import br.com.minhavenda.minhavenda.application.dto.pedido.PedidoDetalhadoDTO;
import br.com.minhavenda.minhavenda.application.service.PedidoService;
import br.com.minhavenda.minhavenda.domain.enums.StatusPedido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller para gerenciar pedidos e checkout.
 *
 * Endpoints do Cliente:
 * - POST /checkout/finalizar - Finalizar checkout
 * - GET /meus-pedidos - Listar meus pedidos
 * - GET /pedidos/{id} - Buscar pedido específico
 * - POST /pedidos/{id}/pagar - Pagar pedido (simulação)
 * - POST /pedidos/{id}/cancelar - Cancelar pedido
 *
 * Endpoints Admin:
 * - GET /admin/pedidos - Listar todos pedidos
 * - GET /admin/pedidos/{id} - Buscar qualquer pedido
 * - POST /pedidos/{id}/enviar - Marcar como enviado
 * - POST /pedidos/{id}/entregar - Marcar como entregue
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Gerenciamento de pedidos e checkout")
@SecurityRequirement(name = "bearer-auth")
public class PedidoController {

    private final PedidoService pedidoService;

    // ========== ENDPOINTS DO CLIENTE ==========

    /**
     * Finaliza o checkout convertendo carrinho em pedido.
     *
     * POST /api/checkout/finalizar
     *
     * Fluxo:
     * 1. Valida carrinho não vazio
     * 2. Valida estoque disponível
     * 3. Cria pedido com status CRIADO
     * 4. Copia itens do carrinho (snapshot de preço e nome)
     * 5. Finaliza carrinho
     *
     * Request:
     * {
     *   "enderecoEntrega": "Rua X, 123, Bairro Y, Cidade Z",
     *   "observacoes": "Entregar após 18h"
     * }
     *
     * Response (201):
     * {
     *   "id": "uuid",
     *   "status": "CRIADO",
     *   "subtotal": 7999.98,
     *   "valorTotal": 7999.98,
     *   "enderecoEntrega": "Rua X, 123...",
     *   "quantidadeItens": 2,
     *   "dataCriacao": "2026-01-16T10:30:00"
     * }
     *
     * @param userDetails usuário logado (do token JWT)
     * @param request dados do checkout
     * @return pedido criado
     */
    @PostMapping("/checkout/finalizar")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Finalizar checkout",
            description = "Converte o carrinho ativo em um pedido com status CRIADO"
    )
    public ResponseEntity<PedidoDTO> finalizarCheckout(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CheckoutRequest request
    ) {
        String email = userDetails.getUsername();
        PedidoDTO pedido = pedidoService.finalizarCheckout(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    /**
     * Lista todos os pedidos do usuário logado.
     * Ordenados por data (mais recente primeiro).
     *
     * GET /api/meus-pedidos
     *
     * Response (200):
     * [
     *   {
     *     "id": "uuid-1",
     *     "status": "PAGO",
     *     "valorTotal": 7999.98,
     *     "quantidadeItens": 2,
     *     "dataCriacao": "2026-01-16T10:30:00"
     *   },
     *   {
     *     "id": "uuid-2",
     *     "status": "ENTREGUE",
     *     "valorTotal": 549.90,
     *     "quantidadeItens": 1,
     *     "dataCriacao": "2026-01-10T15:20:00"
     *   }
     * ]
     *
     * @param userDetails usuário logado
     * @return lista de pedidos
     */
    @GetMapping("/meus-pedidos")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Listar meus pedidos",
            description = "Retorna todos os pedidos do usuário logado"
    )
    public ResponseEntity<List<PedidoDTO>> listarMeusPedidos(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        List<PedidoDTO> pedidos = pedidoService.listarMeusPedidos(email);
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Busca pedido específico do usuário.
     * Inclui lista de itens.
     *
     * GET /api/pedidos/{id}
     *
     * Response (200):
     * {
     *   "id": "uuid",
     *   "status": "PAGO",
     *   "subtotal": 7999.98,
     *   "valorTotal": 7999.98,
     *   "enderecoEntrega": "Rua X, 123...",
     *   "quantidadeItens": 2,
     *   "dataCriacao": "2026-01-16T10:30:00",
     *   "dataPagamento": "2026-01-16T10:35:00",
     *   "itens": [
     *     {
     *       "produtoId": "uuid-produto",
     *       "produtoNome": "Notebook Dell",
     *       "quantidade": 2,
     *       "precoUnitario": 3999.99,
     *       "subtotal": 7999.98
     *     }
     *   ]
     * }
     *
     * Erros:
     * - 404: Pedido não encontrado
     * - 403: Tentando acessar pedido de outro usuário
     *
     * @param userDetails usuário logado
     * @param id ID do pedido
     * @return pedido detalhado
     */
    @GetMapping("/pedidos/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Buscar pedido",
            description = "Retorna detalhes completos de um pedido específico"
    )
    public ResponseEntity<PedidoDetalhadoDTO> buscarPedido(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id
    ) {
        String email = userDetails.getUsername();
        PedidoDetalhadoDTO pedido = pedidoService.buscarPedido(email, id);
        return ResponseEntity.ok(pedido);
    }

    /**
     * Simula pagamento do pedido.
     * Atualiza status de CRIADO para PAGO.
     *
     * POST /api/pedidos/{id}/pagar
     *
     * Validações:
     * - Pedido existe e pertence ao usuário
     * - Status atual é CRIADO
     *
     * Response (200):
     * {
     *   "id": "uuid",
     *   "status": "PAGO",
     *   "dataPagamento": "2026-01-16T10:35:00"
     * }
     *
     * Erros:
     * - 400: Pedido já foi pago
     * - 404: Pedido não encontrado
     *
     * @param userDetails usuário logado
     * @param id ID do pedido
     * @return pedido atualizado
     */
    @PostMapping("/pedidos/{id}/pagar")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Pagar pedido (simulação)",
            description = "Simula pagamento e atualiza status para PAGO"
    )
    public ResponseEntity<PedidoDTO> pagarPedido(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id
    ) {
        String email = userDetails.getUsername();
        PedidoDTO pedido = pedidoService.pagarPedido(email, id);
        return ResponseEntity.ok(pedido);
    }

    /**
     * Cancela um pedido.
     * Apenas pedidos CRIADO ou PAGO podem ser cancelados.
     *
     * POST /api/pedidos/{id}/cancelar
     *
     * Validações:
     * - Pedido existe e pertence ao usuário
     * - Status é CRIADO ou PAGO
     * - Pedidos ENVIADO ou ENTREGUE não podem ser cancelados
     *
     * Response (200):
     * {
     *   "id": "uuid",
     *   "status": "CANCELADO"
     * }
     *
     * Erros:
     * - 400: Pedido não pode ser cancelado (já enviado)
     * - 404: Pedido não encontrado
     *
     * @param userDetails usuário logado
     * @param id ID do pedido
     * @return pedido cancelado
     */
    @PostMapping("/pedidos/{id}/cancelar")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Cancelar pedido",
            description = "Cancela um pedido que ainda não foi enviado"
    )
    public ResponseEntity<PedidoDTO> cancelarPedido(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id
    ) {
        String email = userDetails.getUsername();
        PedidoDTO pedido = pedidoService.cancelarPedido(email, id);
        return ResponseEntity.ok(pedido);
    }

    // ========== ENDPOINTS ADMINISTRATIVOS ==========

    /**
     * Marca pedido como enviado (apenas ADMIN).
     * Atualiza status de PAGO para ENVIADO.
     *
     * POST /api/pedidos/{id}/enviar
     *
     * @param id ID do pedido
     * @return pedido atualizado
     */
    @PostMapping("/pedidos/{id}/enviar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Marcar como enviado (ADMIN)",
            description = "Atualiza status do pedido para ENVIADO"
    )
    public ResponseEntity<PedidoDTO> marcarComoEnviado(@PathVariable UUID id) {
        PedidoDTO pedido = pedidoService.marcarComoEnviado(id);
        return ResponseEntity.ok(pedido);
    }

    /**
     * Marca pedido como entregue (apenas ADMIN).
     * Atualiza status de ENVIADO para ENTREGUE.
     *
     * POST /api/pedidos/{id}/entregar
     *
     * @param id ID do pedido
     * @return pedido atualizado
     */
    @PostMapping("/pedidos/{id}/entregar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Marcar como entregue (ADMIN)",
            description = "Atualiza status do pedido para ENTREGUE"
    )
    public ResponseEntity<PedidoDTO> marcarComoEntregue(@PathVariable UUID id) {
        PedidoDTO pedido = pedidoService.marcarComoEntregue(id);
        return ResponseEntity.ok(pedido);
    }

    /**
     * Lista pedidos por status (apenas ADMIN).
     *
     * GET /api/admin/pedidos?status=PAGO
     *
     * @param status status do pedido
     * @return lista de pedidos
     */
    @GetMapping("/admin/pedidos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar pedidos por status (ADMIN)",
            description = "Retorna todos os pedidos com determinado status"
    )
    public ResponseEntity<List<PedidoDTO>> listarPedidosPorStatus(
            @RequestParam(required = false) StatusPedido status
    ) {
        if (status == null) {
            status = StatusPedido.CRIADO; // Default
        }
        List<PedidoDTO> pedidos = pedidoService.listarPedidosPorStatus(status);
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Busca qualquer pedido por ID (apenas ADMIN).
     *
     * GET /api/admin/pedidos/{id}
     *
     * @param id ID do pedido
     * @return pedido detalhado
     */
    @GetMapping("/admin/pedidos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Buscar pedido (ADMIN)",
            description = "Retorna detalhes de qualquer pedido"
    )
    public ResponseEntity<PedidoDetalhadoDTO> buscarPedidoAdmin(@PathVariable UUID id) {
        PedidoDetalhadoDTO pedido = pedidoService.buscarPedidoAdmin(id);
        return ResponseEntity.ok(pedido);
    }
}
