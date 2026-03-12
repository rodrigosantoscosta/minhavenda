package br.com.minhavenda.minhavenda.application.usecase.pedido;

import br.com.minhavenda.minhavenda.application.event.DomainEventPublisher;
import br.com.minhavenda.minhavenda.domain.entity.Pedido;
import br.com.minhavenda.minhavenda.domain.entity.Usuario;
import br.com.minhavenda.minhavenda.domain.enums.StatusPedido;
import br.com.minhavenda.minhavenda.domain.enums.TipoUsuario;
import br.com.minhavenda.minhavenda.domain.event.DomainEvent;
import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoPagoEvent;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do PagarPedidoUseCase.
 *
 * Usa Mockito para isolar repositório e publisher.
 * Não precisa de Spring context nem banco de dados.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PagarPedidoUseCase - Testes Unitários")
class PagarPedidoUseCaseTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private PagarPedidoUseCase pagarPedidoUseCase;

    private Pedido pedido;
    private UUID pedidoId;

    @BeforeEach
    void setUp() {
        pedidoId = UUID.randomUUID();

        Usuario usuario = Usuario.builder()
            .nome("Teste")
            .email("teste@email.com")
            .senha("hash")
            .tipo(TipoUsuario.CLIENTE)
            .build();

        pedido = new Pedido(
            usuario,
            BigDecimal.valueOf(200),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.valueOf(200),
            "Rua Teste, 456",
            null
        );
    }

    @Test
    @DisplayName("Deve pagar pedido com sucesso")
    void devePagarPedidoComSucesso() {
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pagarPedidoUseCase.executar(pedidoId, "PIX");

        assertThat(resultado.getStatus()).isEqualTo(StatusPedido.PAGO);
        verify(pedidoRepository).save(pedido);
        verify(eventPublisher).publishAll(any());
    }

    @Test
    @DisplayName("Deve publicar PedidoPagoEvent com o método de pagamento correto")
    void devePublicarPedidoPagoEventComMetodoCorreto() {
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        pagarPedidoUseCase.executar(pedidoId, "BOLETO");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventPublisher).publishAll(eventsCaptor.capture());

        List<DomainEvent> publishedEvents = eventsCaptor.getValue();
        assertThat(publishedEvents).hasSize(1);
        assertThat(publishedEvents.get(0)).isInstanceOf(PedidoPagoEvent.class);

        PedidoPagoEvent pagoEvent = (PedidoPagoEvent) publishedEvents.get(0);
        assertThat(pagoEvent.getMetodoPagamento()).isEqualTo("BOLETO");
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido não encontrado")
    void deveLancarExcecaoPedidoNaoEncontrado() {
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagarPedidoUseCase.executar(pedidoId, "PIX"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("não encontrado");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar pagar pedido já pago")
    void deveLancarExcecaoPedidoJaPago() {
        pedido.pagar("PIX");
        pedido.limparEventos();

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pagarPedidoUseCase.executar(pedidoId, "PIX"))
            .isInstanceOf(IllegalStateException.class);

        verify(pedidoRepository, never()).save(any());
        verify(eventPublisher, never()).publishAll(any());
    }
}
