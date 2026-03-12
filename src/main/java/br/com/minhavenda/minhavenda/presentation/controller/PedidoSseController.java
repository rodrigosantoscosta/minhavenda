package br.com.minhavenda.minhavenda.presentation.controller;

import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.UsuarioRepository;
import br.com.minhavenda.minhavenda.infrastructure.sse.SseEmitterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

/**
 * Controller SSE (Server-Sent Events) para atualizacoes em tempo real de pedidos.
 *
 * O frontend conecta uma vez em GET /api/pedidos/stream e recebe eventos
 * automaticamente sempre que o status de um pedido muda (via RabbitMQ consumer).
 *
 * Eventos disponiveis:
 *   pedido.criado    — novo pedido criado
 *   pedido.pago      — pedido confirmado como pago
 *   pedido.enviado   — pedido enviado com codigo de rastreio
 *   pedido.cancelado — pedido cancelado
 *
 * Exemplo de uso no frontend (JavaScript):
 * <pre>
 *   const source = new EventSource('/api/pedidos/stream', {
 *     headers: { Authorization: 'Bearer ' + token }
 *   });
 *   source.addEventListener('pedido.pago', e => console.log(JSON.parse(e.data)));
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos - Stream SSE", description = "Atualizacoes em tempo real via Server-Sent Events")
@SecurityRequirement(name = "bearer-auth")
public class PedidoSseController {

    private final SseEmitterRegistry sseRegistry;
    private final UsuarioRepository usuarioRepository;

    /**
     * Abre uma conexao SSE para o usuario autenticado.
     *
     * GET /api/pedidos/stream
     *
     * O cliente recebe eventos automaticamente quando o status de seus pedidos muda.
     * A conexao expira apos 5 minutos de inatividade — o frontend deve reconectar.
     *
     * @param userDetails usuario autenticado injetado pelo Spring Security
     * @return SseEmitter da conexao aberta
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "Stream de atualizacoes de pedidos (SSE)",
            description = "Abre uma conexao Server-Sent Events. O cliente recebe atualizacoes em tempo real " +
                          "quando o status de seus pedidos muda. Reconecte apos 5 minutos (timeout padrao)."
    )
    @ApiResponse(responseCode = "200", description = "Conexao SSE aberta com sucesso")
    @ApiResponse(responseCode = "401", description = "Nao autenticado")
    public SseEmitter stream(@AuthenticationPrincipal UserDetails userDetails) {
        // Busca o UUID real do usuario no banco para garantir consistencia com os eventos do consumer
        UUID userId = usuarioRepository.findByEmail(userDetails.getUsername())
                .map(u -> u.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado: " + userDetails.getUsername()));

        log.info("[SSE] Nova conexao para usuario: {} (id: {})", userDetails.getUsername(), userId);
        SseEmitter emitter = sseRegistry.register(userId);

        // Envia evento de conexao estabelecida
        try {
            emitter.send(SseEmitter.event()
                    .name("conectado")
                    .data("Conexao SSE estabelecida. Aguardando eventos de pedidos...")
            );
        } catch (IOException e) {
            log.warn("[SSE] Falha ao enviar evento inicial: {}", e.getMessage());
        }

        return emitter;
    }
}
