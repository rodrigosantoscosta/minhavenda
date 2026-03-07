package br.com.minhavenda.minhavenda.infrastructure.messaging.producer;

import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoCanceladoEvent;
import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoCriadoEvent;
import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoEnviadoEvent;
import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoPagoEvent;
import br.com.minhavenda.minhavenda.infrastructure.messaging.dto.PedidoCriadoMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do PedidoRabbitMQProducer.
 *
 * Verifica se cada método chama o RabbitTemplate com a exchange e
 * routing key corretas. Usa Mockito — sem broker real necessário.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoRabbitMQProducer - Testes Unitários")
class PedidoRabbitMQProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PedidoRabbitMQProducer producer;

    private UUID pedidoId;
    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        pedidoId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
    }

    // =========================================================================
    // PEDIDO CRIADO
    // =========================================================================

    @Test
    @DisplayName("Deve publicar PedidoCriadoEvent na exchange correta")
    void devePublicarPedidoCriadoNaExchangeCorreta() {
        PedidoCriadoEvent event = new PedidoCriadoEvent(
            pedidoId, usuarioId, "email@test.com", "João", 150.0, 3
        );

        producer.publicarPedidoCriado(event);

        // Capture the actual call to verify exchange + routing key
        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(
            exchangeCaptor.capture(),
            routingKeyCaptor.capture(),
            (Object) any()
        );

        assertThat(exchangeCaptor.getValue()).isEqualTo("pedidos.exchange");
        assertThat(routingKeyCaptor.getValue()).isEqualTo("pedido.criado");
    }

    // =========================================================================
    // PEDIDO PAGO
    // =========================================================================

    @Test
    @DisplayName("Deve publicar PedidoPagoEvent com routing key correta")
    void devePublicarPedidoPagoComRoutingKeyCorreta() {
        PedidoPagoEvent event = new PedidoPagoEvent(
            pedidoId, usuarioId, "email@test.com", 150.0, "PIX"
        );

        producer.publicarPedidoPago(event);

        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(
            anyString(),
            routingKeyCaptor.capture(),
            (Object) any()
        );

        assertThat(routingKeyCaptor.getValue()).isEqualTo("pedido.pago");
    }

    // =========================================================================
    // PEDIDO ENVIADO
    // =========================================================================

    @Test
    @DisplayName("Deve publicar PedidoEnviadoEvent com routing key correta")
    void devePublicarPedidoEnviadoComRoutingKeyCorreta() {
        PedidoEnviadoEvent event = new PedidoEnviadoEvent(
            pedidoId, usuarioId, "João", "email@test.com",
            "11999999999", "BR123456", "Correios"
        );

        producer.publicarPedidoEnviado(event);

        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(
            anyString(),
            routingKeyCaptor.capture(),
            (Object) any()
        );

        assertThat(routingKeyCaptor.getValue()).isEqualTo("pedido.enviado");
    }

    // =========================================================================
    // PEDIDO CANCELADO
    // =========================================================================

    @Test
    @DisplayName("Deve publicar PedidoCanceladoEvent com routing key correta")
    void devePublicarPedidoCanceladoComRoutingKeyCorreta() {
        PedidoCanceladoEvent event = new PedidoCanceladoEvent(
            pedidoId, usuarioId, "email@test.com", "Desistência"
        );

        producer.publicarPedidoCancelado(event);

        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(
            anyString(),
            routingKeyCaptor.capture(),
            (Object) any()
        );

        assertThat(routingKeyCaptor.getValue()).isEqualTo("pedido.cancelado");
    }

    // =========================================================================
    // RESILIÊNCIA — Falha no RabbitMQ não deve propagar
    // =========================================================================

    @Test
    @DisplayName("Não deve propagar exceção quando RabbitMQ falhar")
    void naoDevePropagarExcecaoQuandoRabbitMQFalhar() {
        doThrow(new RuntimeException("Broker indisponível"))
            .when(rabbitTemplate)
            .convertAndSend(anyString(), anyString(), (Object) any());

        PedidoCriadoEvent event = new PedidoCriadoEvent(
            pedidoId, usuarioId, "email@test.com", "João", 150.0, 2
        );

        // Não deve lançar exceção — o producer swallows erros do broker
        assertThatCode(() -> producer.publicarPedidoCriado(event))
            .doesNotThrowAnyException();
    }
}
