package br.com.minhavenda.minhavenda.infrastructure.messaging.consumer;

import br.com.minhavenda.minhavenda.infrastructure.messaging.dto.*;
import br.com.minhavenda.minhavenda.infrastructure.sse.SseEmitterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do PedidoRabbitMQConsumer.
 *
 * Verifica:
 * - Processamento sem lançar exceção (caminho feliz)
 * - Delegação ao SseEmitterRegistry com os dados corretos
 * - Relançamento de exceção para DLQ (caminho de erro)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoRabbitMQConsumer - Testes Unitários")
class PedidoRabbitMQConsumerTest {

    @Mock
    private SseEmitterRegistry sseRegistry;

    @InjectMocks
    private PedidoRabbitMQConsumer consumer;

    private UUID pedidoId;
    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        pedidoId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
    }

    // =========================================================================
    // CAMINHO FELIZ
    // =========================================================================

    @Test
    @DisplayName("onPedidoCriado deve processar mensagem sem lançar exceção")
    void onPedidoCriadoDeveProcessarSemExcecao() {
        PedidoCriadoMessage message = PedidoCriadoMessage.builder()
            .eventId(UUID.randomUUID())
            .pedidoId(pedidoId)
            .usuarioId(usuarioId)
            .emailUsuario("user@test.com")
            .nomeUsuario("João")
            .valorTotal(150.0)
            .quantidadeItens(2)
            .ocorridoEm(Instant.now())
            .build();

        assertThatCode(() -> consumer.onPedidoCriado(message))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("onPedidoCriado deve notificar SSE com evento correto")
    void onPedidoCriadoDeveNotificarSse() {
        PedidoCriadoMessage message = PedidoCriadoMessage.builder()
            .eventId(UUID.randomUUID())
            .pedidoId(pedidoId)
            .usuarioId(usuarioId)
            .emailUsuario("user@test.com")
            .nomeUsuario("João")
            .valorTotal(150.0)
            .quantidadeItens(2)
            .ocorridoEm(Instant.now())
            .build();

        consumer.onPedidoCriado(message);

        verify(sseRegistry).sendEvent(eq(usuarioId), eq("pedido.criado"), eq(message));
    }

    @Test
    @DisplayName("onPedidoPago deve processar mensagem sem lançar exceção")
    void onPedidoPagoDeveProcessarSemExcecao() {
        PedidoPagoMessage message = PedidoPagoMessage.builder()
            .eventId(UUID.randomUUID())
            .pedidoId(pedidoId)
            .usuarioId(usuarioId)
            .emailUsuario("user@test.com")
            .valorPago(150.0)
            .metodoPagamento("PIX")
            .ocorridoEm(Instant.now())
            .build();

        assertThatCode(() -> consumer.onPedidoPago(message))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("onPedidoPago deve notificar SSE com evento correto")
    void onPedidoPagoDeveNotificarSse() {
        PedidoPagoMessage message = PedidoPagoMessage.builder()
            .eventId(UUID.randomUUID())
            .pedidoId(pedidoId)
            .usuarioId(usuarioId)
            .emailUsuario("user@test.com")
            .valorPago(150.0)
            .metodoPagamento("PIX")
            .ocorridoEm(Instant.now())
            .build();

        consumer.onPedidoPago(message);

        verify(sseRegistry).sendEvent(eq(usuarioId), eq("pedido.pago"), eq(message));
    }

    @Test
    @DisplayName("onPedidoEnviado deve processar mensagem sem lançar exceção")
    void onPedidoEnviadoDeveProcessarSemExcecao() {
        PedidoEnviadoMessage message = PedidoEnviadoMessage.builder()
            .eventId(UUID.randomUUID())
            .pedidoId(pedidoId)
            .usuarioId(usuarioId)
            .nomeUsuario("João")
            .emailUsuario("user@test.com")
            .telefone("11999999999")
            .codigoRastreio("BR123456789")
            .transportadora("Correios")
            .ocorridoEm(Instant.now())
            .build();

        assertThatCode(() -> consumer.onPedidoEnviado(message))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("onPedidoEnviado deve notificar SSE com evento correto")
    void onPedidoEnviadoDeveNotificarSse() {
        PedidoEnviadoMessage message = PedidoEnviadoMessage.builder()
            .eventId(UUID.randomUUID())
            .pedidoId(pedidoId)
            .usuarioId(usuarioId)
            .nomeUsuario("João")
            .emailUsuario("user@test.com")
            .telefone("11999999999")
            .codigoRastreio("BR123456789")
            .transportadora("Correios")
            .ocorridoEm(Instant.now())
            .build();

        consumer.onPedidoEnviado(message);

        verify(sseRegistry).sendEvent(eq(usuarioId), eq("pedido.enviado"), eq(message));
    }

    @Test
    @DisplayName("onPedidoCancelado deve processar mensagem sem lançar exceção")
    void onPedidoCanceladoDeveProcessarSemExcecao() {
        PedidoCanceladoMessage message = PedidoCanceladoMessage.builder()
            .eventId(UUID.randomUUID())
            .pedidoId(pedidoId)
            .usuarioId(usuarioId)
            .emailUsuario("user@test.com")
            .motivo("Cliente desistiu")
            .ocorridoEm(Instant.now())
            .build();

        assertThatCode(() -> consumer.onPedidoCancelado(message))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("onPedidoCancelado deve notificar SSE com evento correto")
    void onPedidoCanceladoDeveNotificarSse() {
        PedidoCanceladoMessage message = PedidoCanceladoMessage.builder()
            .eventId(UUID.randomUUID())
            .pedidoId(pedidoId)
            .usuarioId(usuarioId)
            .emailUsuario("user@test.com")
            .motivo("Cliente desistiu")
            .ocorridoEm(Instant.now())
            .build();

        consumer.onPedidoCancelado(message);

        verify(sseRegistry).sendEvent(eq(usuarioId), eq("pedido.cancelado"), eq(message));
    }

    // =========================================================================
    // CAMINHO DE ERRO — exceção deve propagar para a DLQ
    // =========================================================================

    @Test
    @DisplayName("Deve relançar exceção quando SSE registry falhar (para roteamento à DLQ)")
    void deveRelancarExcecaoQuandoSseRegistryFalhar() {
        doThrow(new RuntimeException("SSE failure"))
            .when(sseRegistry).sendEvent(any(), anyString(), any());

        PedidoCriadoMessage message = PedidoCriadoMessage.builder()
            .eventId(UUID.randomUUID())
            .pedidoId(pedidoId)
            .usuarioId(usuarioId)
            .emailUsuario("user@test.com")
            .nomeUsuario("João")
            .valorTotal(150.0)
            .quantidadeItens(2)
            .ocorridoEm(Instant.now())
            .build();

        assertThatThrownBy(() -> consumer.onPedidoCriado(message))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("SSE failure");
    }
}
