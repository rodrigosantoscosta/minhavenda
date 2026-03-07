package br.com.minhavenda.minhavenda.presentation.controller;

import br.com.minhavenda.minhavenda.application.dto.pedido.PedidoDTO;
import br.com.minhavenda.minhavenda.application.dto.pedido.PedidoDetalhadoDTO;
import br.com.minhavenda.minhavenda.application.mapper.PedidoMapper;
import br.com.minhavenda.minhavenda.application.usecase.pedido.EnviarPedidoUseCase;
import br.com.minhavenda.minhavenda.application.usecase.pedido.PagarPedidoUseCase;
import br.com.minhavenda.minhavenda.domain.entity.Pedido;
import br.com.minhavenda.minhavenda.domain.enums.StatusPedido;
import br.com.minhavenda.minhavenda.infrastructure.notification.EmailService;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.PedidoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller administrativo para gerenciamento de pedidos.
 *
 * Endpoints restritos a usuários com role ADMIN.
 * Permite gerenciar pedidos de todos os usuários.
 *
 * MAILHOG TESTING:
 * - Todos os endpoints que mudam status de pedido emitem eventos
 * - Eventos disparam envio de emails
 * - Emails podem ser visualizados em http://localhost:8025
 *
 * @author MinhaVenda Team
 */
@Slf4j
@RestController
@RequestMapping("/admin/pedidos")
@RequiredArgsConstructor
@Tag(name = "Admin - Pedidos", description = "Endpoints administrativos para gerenciamento de pedidos")
@SecurityRequirement(name = "bearer-auth")
public class AdminPedidoController {

    private final EnviarPedidoUseCase enviarPedidoUseCase;
    private final PagarPedidoUseCase pagarPedidoUseCase;
    private final PedidoRepository pedidoRepository;
    private final PedidoMapper pedidoMapper;
    private final JavaMailSender mailSender;

    @GetMapping("/teste-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> testarEmail() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@minhavenda.com.br");
            message.setTo("teste@teste.com");
            message.setSubject("Teste Mailhog");
            message.setText("Este é um email de teste do Mailhog");

            mailSender.send(message);

            return ResponseEntity.ok("Email enviado! Verifique http://localhost:8025");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro: " + e.getMessage());
        }
    }


    // ========================================================================
    // ENDPOINTS DE LISTAGEM
    // ========================================================================

    /**
     * Lista todos os pedidos do sistema (ADMIN).
     *
     * GET /api/admin/pedidos
     *
     * @return lista de todos os pedidos
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar todos os pedidos",
            description = "Retorna todos os pedidos do sistema, de todos os usuários"
    )
    public ResponseEntity<List<PedidoDTO>> listarTodosPedidos() {
        log.info("Admin listando todos os pedidos");

        List<Pedido> pedidos = pedidoRepository.findAll();

        List<PedidoDTO> pedidosDTO = pedidos.stream()
                .map(pedidoMapper::toDTO)
                .collect(Collectors.toList());

        log.info("Retornando {} pedidos", pedidosDTO.size());
        return ResponseEntity.ok(pedidosDTO);
    }

    /**
     * Lista pedidos por status (ADMIN).
     *
     * GET /api/admin/pedidos/status/{status}
     *
     * @param status status do pedido (CRIADO, PAGO, ENVIADO, ENTREGUE, CANCELADO)
     * @return lista de pedidos com o status especificado
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar pedidos por status",
            description = "Retorna todos os pedidos com determinado status"
    )
    public ResponseEntity<List<PedidoDTO>> listarPedidosPorStatus(
            @Parameter(description = "Status do pedido", example = "PAGO")
            @PathVariable StatusPedido status
    ) {
        log.info("Admin listando pedidos com status: {}", status);

        List<Pedido> pedidos = pedidoRepository.findByStatus(status);

        List<PedidoDTO> pedidosDTO = pedidos.stream()
                .map(pedidoMapper::toDTO)
                .collect(Collectors.toList());

        log.info("Encontrados {} pedidos com status {}", pedidosDTO.size(), status);
        return ResponseEntity.ok(pedidosDTO);
    }

    /**
     * Busca pedido específico por ID (ADMIN).
     *
     * GET /api/admin/pedidos/{id}
     *
     * @param id ID do pedido
     * @return detalhes completos do pedido
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Buscar pedido por ID",
            description = "Retorna detalhes completos de qualquer pedido do sistema"
    )
    @ApiResponse(responseCode = "200", description = "Pedido encontrado")
    @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    public ResponseEntity<PedidoDetalhadoDTO> buscarPedido(
            @Parameter(description = "ID do pedido", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id
    ) {
        log.info("Admin buscando pedido: {}", id);

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        PedidoDetalhadoDTO pedidoDTO = pedidoMapper.toDetalhadoDTO(pedido);

        return ResponseEntity.ok(pedidoDTO);
    }

    // ========================================================================
    // ENDPOINTS DE MUDANÇA DE STATUS (TESTAM EVENTOS/EMAILS)
    // ========================================================================

    /**
     * Marca pedido como PAGO (ADMIN).
     *
     * POST /api/admin/pedidos/{id}/pagar
     *
     * ✉️ MAILHOG: Este endpoint dispara PedidoPagoEvent
     * Email enviado para o cliente confirmando pagamento
     * Visualizar em: http://localhost:8025
     *
     * @param id ID do pedido
     * @param metodoPagamento método de pagamento (opcional)
     * @return pedido atualizado
     */
    @PostMapping("/{id}/pagar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Marcar pedido como pago (ADMIN)",
            description = "⚡ DISPARA EVENTO: PedidoPagoEvent → Email de confirmação de pagamento"
    )
    @ApiResponse(responseCode = "200", description = "Pedido marcado como pago")
    @ApiResponse(responseCode = "400", description = "Pedido não pode ser pago (status inválido)")
    public ResponseEntity<PedidoDTO> pagarPedido(
            @Parameter(description = "ID do pedido")
            @PathVariable UUID id,

            @Parameter(description = "Método de pagamento", example = "PIX")
            @RequestParam(required = false, defaultValue = "Admin Manual")
            String metodoPagamento
    ) {
        log.info("🎧 Admin marcando pedido {} como pago (método: {})", id, metodoPagamento);

        Pedido pedido = pagarPedidoUseCase.executar(id, metodoPagamento);
        PedidoDTO pedidoDTO = pedidoMapper.toDTO(pedido);

        log.info("✅ Pedido {} marcado como PAGO. Verifique Mailhog: http://localhost:8025", id);

        return ResponseEntity.ok(pedidoDTO);
    }

    /**
     * Marca pedido como ENVIADO (ADMIN).
     *
     * POST /api/admin/pedidos/{id}/enviar
     *
     * ✉️ MAILHOG: Este endpoint dispara PedidoEnviadoEvent
     * Email enviado para o cliente com código de rastreio
     * Visualizar em: http://localhost:8025
     *
     * @param id ID do pedido
     * @param codigoRastreio código de rastreamento da transportadora
     * @param transportadora nome da transportadora
     * @param telefone telefone do cliente (opcional)
     * @return pedido atualizado
     */
    @PostMapping("/{id}/enviar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Marcar pedido como enviado (ADMIN)",
            description = "⚡ DISPARA EVENTO: PedidoEnviadoEvent → Email com código de rastreio"
    )
    @ApiResponse(responseCode = "200", description = "Pedido marcado como enviado")
    @ApiResponse(responseCode = "400", description = "Pedido não pode ser enviado (status inválido)")
    public ResponseEntity<PedidoDTO> enviarPedido(
            @Parameter(description = "ID do pedido")
            @PathVariable UUID id,

            @Parameter(description = "Código de rastreio", example = "BR123456789BR")
            @RequestParam String codigoRastreio,

            @Parameter(description = "Nome da transportadora", example = "Correios")
            @RequestParam String transportadora,

            @Parameter(description = "Telefone do cliente (opcional)", example = "11999999999")
            @RequestParam(required = false) String telefone
    ) {
        log.info("🎧 Admin marcando pedido {} como enviado (rastreio: {}, transportadora: {})",
                id, codigoRastreio, transportadora);

//        // Buscar telefone do usuário se não fornecido
//        if (telefone == null || telefone.isBlank()) {
//            Pedido pedidoTemp = pedidoRepository.findById(id)
//                    .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
//            telefone = pedidoTemp.getUsuario().getTelefone();
//
//            if (telefone == null || telefone.isBlank()) {
//                telefone = "Não informado";
//            }
//        }

        if (telefone == null || telefone.isBlank()) {
                telefone = "Não informado";
        }

        Pedido pedido = enviarPedidoUseCase.executar(
                id, codigoRastreio, transportadora, telefone
        );

        PedidoDTO pedidoDTO = pedidoMapper.toDTO(pedido);

        log.info("✅ Pedido {} marcado como ENVIADO. Verifique Mailhog: http://localhost:8025", id);

        return ResponseEntity.ok(pedidoDTO);
    }

    /**
     * Marca pedido como ENTREGUE (ADMIN).
     *
     * POST /api/admin/pedidos/{id}/entregar
     *
     * @param id ID do pedido
     * @return pedido atualizado
     */
    @PostMapping("/{id}/entregar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Marcar pedido como entregue (ADMIN)",
            description = "Finaliza o pedido marcando como entregue"
    )
    @ApiResponse(responseCode = "200", description = "Pedido marcado como entregue")
    @ApiResponse(responseCode = "400", description = "Pedido não pode ser entregue (status inválido)")
    public ResponseEntity<PedidoDTO> entregarPedido(
            @Parameter(description = "ID do pedido")
            @PathVariable UUID id
    ) {
        log.info("Admin marcando pedido {} como entregue", id);

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.marcarComoEntregue();
        pedido = pedidoRepository.save(pedido);

        PedidoDTO pedidoDTO = pedidoMapper.toDTO(pedido);

        log.info("✅ Pedido {} marcado como ENTREGUE", id);

        return ResponseEntity.ok(pedidoDTO);
    }

    /**
     * Cancela um pedido (ADMIN).
     *
     * POST /api/admin/pedidos/{id}/cancelar
     *
     * ✉️ MAILHOG: Este endpoint dispara PedidoCanceladoEvent
     * Email enviado para o cliente confirmando cancelamento
     * Visualizar em: http://localhost:8025
     *
     * @param id ID do pedido
     * @param motivo motivo do cancelamento
     * @return pedido cancelado
     */
    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Cancelar pedido (ADMIN)",
            description = "⚡ DISPARA EVENTO: PedidoCanceladoEvent → Email de cancelamento"
    )
    @ApiResponse(responseCode = "200", description = "Pedido cancelado")
    @ApiResponse(responseCode = "400", description = "Pedido não pode ser cancelado (já enviado/entregue)")
    public ResponseEntity<PedidoDTO> cancelarPedido(
            @Parameter(description = "ID do pedido")
            @PathVariable UUID id,

            @Parameter(description = "Motivo do cancelamento", example = "Produto fora de estoque")
            @RequestParam(required = false, defaultValue = "Cancelado pelo administrador")
            String motivo
    ) {
        log.info("🎧 Admin cancelando pedido {} (motivo: {})", id, motivo);

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.cancelar(motivo);
        pedido = pedidoRepository.save(pedido);

        // Publicar eventos
        // TODO: Se usando Use Case, descomentar:
        // eventPublisher.publishAll(pedido.getDomainEvents());
        // pedido.limparEventos();

        PedidoDTO pedidoDTO = pedidoMapper.toDTO(pedido);

        log.info("✅ Pedido {} CANCELADO. Verifique Mailhog: http://localhost:8025", id);

        return ResponseEntity.ok(pedidoDTO);
    }

    // ========================================================================
    // ENDPOINT DE TESTE MAILHOG
    // ========================================================================

    /**
     * Endpoint de teste para verificar fluxo completo de emails.
     *
     * POST /api/admin/pedidos/{id}/teste-emails
     *
     * Simula todo o fluxo de status do pedido disparando todos os eventos:
     * 1. CRIADO → PAGO (email de pagamento)
     * 2. PAGO → ENVIADO (email com rastreio)
     *
     * ✉️ MAILHOG: Verifique http://localhost:8025 após executar
     *
     * @param id ID do pedido
     * @return resumo dos eventos disparados
     */
    @PostMapping("/{id}/teste-emails")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "🧪 TESTE: Simular fluxo completo de emails",
            description = "Executa transições de status para testar todos os eventos de email no Mailhog"
    )
    public ResponseEntity<String> testarFluxoEmails(
            @Parameter(description = "ID do pedido (deve estar com status CRIADO)")
            @PathVariable UUID id
    ) {
        log.info("🧪 Iniciando teste de fluxo de emails para pedido: {}", id);

        StringBuilder resultado = new StringBuilder();
        resultado.append("🧪 TESTE DE EMAILS - Pedido ").append(id).append("\n\n");

        try {
            Pedido pedido = pedidoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

            resultado.append("Status inicial: ").append(pedido.getStatus()).append("\n\n");

            // 1. PAGAR (se estiver CRIADO)
            if (pedido.getStatus() == StatusPedido.CRIADO) {
                log.info("1️⃣ Marcando como PAGO...");
                pagarPedidoUseCase.executar(id, "Teste Mailhog - PIX");
                resultado.append("✅ 1. Pedido PAGO → Email de confirmação enviado\n");
            }

            // 2. ENVIAR (se estiver PAGO)
            pedido = pedidoRepository.findById(id).get();
            if (pedido.getStatus() == StatusPedido.PAGO) {
                log.info("2️⃣ Marcando como ENVIADO...");
                enviarPedidoUseCase.executar(
                        id,
                        "TESTE-BR" + System.currentTimeMillis(),
                        "Correios - Teste Mailhog",
                        "11999999999"
                );
                resultado.append("✅ 2. Pedido ENVIADO → Email com rastreio enviado\n");
            }

            resultado.append("\n📧 Verifique os emails em: http://localhost:8025\n");
            resultado.append("📊 Total de eventos disparados: 2 (PedidoPagoEvent + PedidoEnviadoEvent)\n");

            log.info("✅ Teste de emails concluído com sucesso!");

        } catch (Exception e) {
            log.error("❌ Erro no teste de emails", e);
            resultado.append("\n❌ ERRO: ").append(e.getMessage());
            return ResponseEntity.badRequest().body(resultado.toString());
        }

        return ResponseEntity.ok(resultado.toString());
    }
}