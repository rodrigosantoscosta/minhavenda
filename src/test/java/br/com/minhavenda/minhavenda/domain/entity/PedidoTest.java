package br.com.minhavenda.minhavenda.domain.entity;

import br.com.minhavenda.minhavenda.domain.enums.StatusPedido;
import br.com.minhavenda.minhavenda.domain.enums.TipoUsuario;
import br.com.minhavenda.minhavenda.domain.event.DomainEvent;
import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoCanceladoEvent;
import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoEnviadoEvent;
import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoPagoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários da entidade Pedido (Aggregate Root).
 *
 * Cobre:
 * - Criação com validações
 * - Ciclo de vida de status (pagar, enviar, cancelar, entregar)
 * - Emissão de Domain Events
 * - Regras de negócio
 */
@DisplayName("Pedido - Testes Unitários")
class PedidoTest {

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = criarUsuario();
    }

    // =========================================================================
    // CRIAÇÃO
    // =========================================================================

    @Nested
    @DisplayName("Criação de Pedido")
    class CriacaoPedido {

        @Test
        @DisplayName("Deve criar pedido com dados válidos")
        void deveCriarPedidoComDadosValidos() {
            Pedido pedido = criarPedidoValido();

            assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CRIADO);
            assertThat(pedido.getUsuario()).isEqualTo(usuario);
            assertThat(pedido.getEnderecoEntrega()).isEqualTo("Rua Teste, 123");
            assertThat(pedido.getDataCriacao()).isNotNull();
            assertThat(pedido.getDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário for nulo")
        void deveLancarExcecaoQuandoUsuarioNulo() {
            assertThatThrownBy(() ->
                new Pedido(null, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.TEN, "Rua Teste", null)
            )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Usuário não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção quando endereço for vazio")
        void deveLancarExcecaoQuandoEnderecoVazio() {
            assertThatThrownBy(() ->
                new Pedido(usuario, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.TEN, "", null)
            )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Endereço de entrega é obrigatório");
        }

        @Test
        @DisplayName("Deve lançar exceção quando endereço for nulo")
        void deveLancarExcecaoQuandoEnderecoNulo() {
            assertThatThrownBy(() ->
                new Pedido(usuario, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.TEN, null, null)
            )
            .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando subtotal for negativo")
        void deveLancarExcecaoQuandoSubtotalNegativo() {
            assertThatThrownBy(() ->
                new Pedido(usuario, BigDecimal.valueOf(-1), BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.TEN, "Rua Teste", null)
            )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Subtotal inválido");
        }
    }

    // =========================================================================
    // PAGAMENTO
    // =========================================================================

    @Nested
    @DisplayName("Pagamento de Pedido")
    class PagamentoPedido {

        @Test
        @DisplayName("Deve pagar pedido com status CRIADO")
        void devePagarPedidoCriado() {
            Pedido pedido = criarPedidoValido();

            pedido.pagar("PIX");

            assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PAGO);
            assertThat(pedido.getDataPagamento()).isNotNull();
        }

        @Test
        @DisplayName("Deve emitir PedidoPagoEvent ao pagar")
        void deveEmitirEventoAoPagar() {
            Pedido pedido = criarPedidoValido();

            pedido.pagar("CARTAO_CREDITO");

            List<DomainEvent> eventos = pedido.getDomainEvents();
            assertThat(eventos).hasSize(1);
            assertThat(eventos.get(0)).isInstanceOf(PedidoPagoEvent.class);

            PedidoPagoEvent evento = (PedidoPagoEvent) eventos.get(0);
            assertThat(evento.getMetodoPagamento()).isEqualTo("CARTAO_CREDITO");
        }

        @Test
        @DisplayName("Deve lançar exceção ao pagar pedido já pago")
        void deveLancarExcecaoAoPagarPedidoJaPago() {
            Pedido pedido = criarPedidoValido();
            pedido.pagar("PIX");

            assertThatThrownBy(() -> pedido.pagar("PIX"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CRIADO");
        }

        @Test
        @DisplayName("Deve lançar exceção ao pagar pedido cancelado")
        void deveLancarExcecaoAoPagarPedidoCancelado() {
            Pedido pedido = criarPedidoValido();
            pedido.cancelar("Teste");

            assertThatThrownBy(() -> pedido.pagar("PIX"))
                .isInstanceOf(IllegalStateException.class);
        }
    }

    // =========================================================================
    // ENVIO
    // =========================================================================

    @Nested
    @DisplayName("Envio de Pedido")
    class EnvioPedido {

        @Test
        @DisplayName("Deve enviar pedido com status PAGO")
        void deveEnviarPedidoPago() {
            Pedido pedido = criarPedidoValido();
            pedido.pagar("PIX");
            pedido.limparEventos();

            pedido.enviar("BR123456789", "Correios", "11999999999");

            assertThat(pedido.getStatus()).isEqualTo(StatusPedido.ENVIADO);
            assertThat(pedido.getCodigoRastreio()).isEqualTo("BR123456789");
            assertThat(pedido.getTransportadora()).isEqualTo("Correios");
            assertThat(pedido.getDataEnvio()).isNotNull();
        }

        @Test
        @DisplayName("Deve emitir PedidoEnviadoEvent ao enviar")
        void deveEmitirEventoAoEnviar() {
            Pedido pedido = criarPedidoValido();
            pedido.pagar("PIX");
            pedido.limparEventos();

            pedido.enviar("BR123456789", "Correios", "11999999999");

            List<DomainEvent> eventos = pedido.getDomainEvents();
            assertThat(eventos).hasSize(1);
            assertThat(eventos.get(0)).isInstanceOf(PedidoEnviadoEvent.class);
        }

        @Test
        @DisplayName("Deve lançar exceção ao enviar pedido não pago")
        void deveLancarExcecaoAoEnviarPedidoNaoPago() {
            Pedido pedido = criarPedidoValido();

            assertThatThrownBy(() -> pedido.enviar("BR123456789", "Correios", null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pagos");
        }

        @Test
        @DisplayName("Deve lançar exceção quando código de rastreio for vazio")
        void deveLancarExcecaoQuandoCodigoRastreioVazio() {
            Pedido pedido = criarPedidoValido();
            pedido.pagar("PIX");

            assertThatThrownBy(() -> pedido.enviar("", "Correios", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código de rastreio é obrigatório");
        }

        @Test
        @DisplayName("Deve lançar exceção quando transportadora for vazia")
        void deveLancarExcecaoQuandoTransportadoraVazia() {
            Pedido pedido = criarPedidoValido();
            pedido.pagar("PIX");

            assertThatThrownBy(() -> pedido.enviar("BR123456789", "", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transportadora é obrigatória");
        }
    }

    // =========================================================================
    // CANCELAMENTO
    // =========================================================================

    @Nested
    @DisplayName("Cancelamento de Pedido")
    class CancelamentoPedido {

        @Test
        @DisplayName("Deve cancelar pedido com status CRIADO")
        void deveCancelarPedidoCriado() {
            Pedido pedido = criarPedidoValido();

            pedido.cancelar("Desistência do cliente");

            assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CANCELADO);
        }

        @Test
        @DisplayName("Deve cancelar pedido com status PAGO")
        void deveCancelarPedidoPago() {
            Pedido pedido = criarPedidoValido();
            pedido.pagar("PIX");
            pedido.limparEventos();

            pedido.cancelar("Produto esgotado");

            assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CANCELADO);
        }

        @Test
        @DisplayName("Deve emitir PedidoCanceladoEvent ao cancelar")
        void deveEmitirEventoAoCancelar() {
            Pedido pedido = criarPedidoValido();

            pedido.cancelar("Motivo qualquer");

            List<DomainEvent> eventos = pedido.getDomainEvents();
            assertThat(eventos).hasSize(1);
            assertThat(eventos.get(0)).isInstanceOf(PedidoCanceladoEvent.class);

            PedidoCanceladoEvent evento = (PedidoCanceladoEvent) eventos.get(0);
            assertThat(evento.getMotivo()).isEqualTo("Motivo qualquer");
        }

        @Test
        @DisplayName("Deve lançar exceção ao cancelar pedido enviado")
        void deveLancarExcecaoAoCancelarPedidoEnviado() {
            Pedido pedido = criarPedidoValido();
            pedido.pagar("PIX");
            pedido.enviar("BR123456789", "Correios", null);

            assertThatThrownBy(() -> pedido.cancelar("Motivo"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enviados");
        }

        @Test
        @DisplayName("Deve lançar exceção ao cancelar pedido já cancelado")
        void deveLancarExcecaoAoCancelarPedidoJaCancelado() {
            Pedido pedido = criarPedidoValido();
            pedido.cancelar("Primeiro cancelamento");

            assertThatThrownBy(() -> pedido.cancelar("Segundo cancelamento"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("já está cancelado");
        }
    }

    // =========================================================================
    // ENTREGA
    // =========================================================================

    @Nested
    @DisplayName("Entrega de Pedido")
    class EntregaPedido {

        @Test
        @DisplayName("Deve marcar como entregue pedido enviado")
        void deveMarcarComoEntregue() {
            Pedido pedido = criarPedidoValido();
            pedido.pagar("PIX");
            pedido.enviar("BR123456789", "Correios", null);

            pedido.marcarComoEntregue();

            assertThat(pedido.getStatus()).isEqualTo(StatusPedido.ENTREGUE);
            assertThat(pedido.getDataEntrega()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção ao entregar pedido não enviado")
        void deveLancarExcecaoAoEntregarPedidoNaoEnviado() {
            Pedido pedido = criarPedidoValido();
            pedido.pagar("PIX");

            assertThatThrownBy(() -> pedido.marcarComoEntregue())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enviados");
        }
    }

    // =========================================================================
    // DOMAIN EVENTS
    // =========================================================================

    @Nested
    @DisplayName("Domain Events")
    class DomainEvents {

        @Test
        @DisplayName("Deve limpar eventos após limparEventos()")
        void deveLimparEventos() {
            Pedido pedido = criarPedidoValido();
            pedido.pagar("PIX");
            assertThat(pedido.getDomainEvents()).isNotEmpty();

            pedido.limparEventos();

            assertThat(pedido.getDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("getDomainEvents deve retornar lista imutável")
        void deveRetornarListaImutavel() {
            Pedido pedido = criarPedidoValido();

            List<DomainEvent> eventos = pedido.getDomainEvents();

            assertThatThrownBy(() -> eventos.add(null))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // =========================================================================
    // CÁLCULOS
    // =========================================================================

    @Nested
    @DisplayName("Cálculos de Valor")
    class CalculosValor {

        @Test
        @DisplayName("Deve ter valor total correto após criação")
        void deveTerValorTotalCorreto() {
            Pedido pedido = criarPedidoValido();
            // subtotal=100, frete=15, desconto=10 → total=105
            assertThat(pedido.getValorTotal()).isEqualByComparingTo(BigDecimal.valueOf(105));
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private Usuario criarUsuario() {
        return Usuario.builder()
            .nome("João Teste")
            .email("joao@teste.com")
            .senha("senha_hash")
            .tipo(TipoUsuario.CLIENTE)
            .build();
    }

    private Pedido criarPedidoValido() {
        return new Pedido(
            usuario,
            BigDecimal.valueOf(100),
            BigDecimal.valueOf(15),
            BigDecimal.valueOf(10),
            BigDecimal.valueOf(105),
            "Rua Teste, 123",
            null
        );
    }
}
