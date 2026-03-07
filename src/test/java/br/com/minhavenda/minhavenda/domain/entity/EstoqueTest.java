package br.com.minhavenda.minhavenda.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários da entidade Estoque.
 *
 * Cobre:
 * - adicionar, remover, reservar, liberar, ajustar
 * - Regras de estoque insuficiente
 * - Helpers (temEstoqueSuficiente, isEstoqueBaixo, isSemEstoque)
 */
@DisplayName("Estoque - Testes Unitários")
class EstoqueTest {

    private Estoque estoque;

    @BeforeEach
    void setUp() {
        estoque = Estoque.builder()
            .quantidade(100)
            .build();
    }

    // =========================================================================
    // ADICIONAR
    // =========================================================================

    @Nested
    @DisplayName("Adicionar estoque")
    class AdicionarEstoque {

        @Test
        @DisplayName("Deve adicionar quantidade corretamente")
        void deveAdicionarQuantidade() {
            estoque.adicionar(50);
            assertThat(estoque.getQuantidade()).isEqualTo(150);
        }

        @Test
        @DisplayName("Deve lançar exceção quando quantidade for zero")
        void deveLancarExcecaoQuantidadeZero() {
            assertThatThrownBy(() -> estoque.adicionar(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maior que zero");
        }

        @Test
        @DisplayName("Deve lançar exceção quando quantidade for negativa")
        void deveLancarExcecaoQuantidadeNegativa() {
            assertThatThrownBy(() -> estoque.adicionar(-5))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando quantidade for nula")
        void deveLancarExcecaoQuantidadeNula() {
            assertThatThrownBy(() -> estoque.adicionar(null))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // =========================================================================
    // REMOVER
    // =========================================================================

    @Nested
    @DisplayName("Remover estoque")
    class RemoverEstoque {

        @Test
        @DisplayName("Deve remover quantidade corretamente")
        void deveRemoverQuantidade() {
            estoque.remover(30);
            assertThat(estoque.getQuantidade()).isEqualTo(70);
        }

        @Test
        @DisplayName("Deve remover toda a quantidade disponível")
        void deveRemoverTudoDisponivel() {
            estoque.remover(100);
            assertThat(estoque.getQuantidade()).isEqualTo(0);
        }

        @Test
        @DisplayName("Deve lançar exceção quando estoque insuficiente")
        void deveLancarExcecaoEstoqueInsuficiente() {
            assertThatThrownBy(() -> estoque.remover(101))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Estoque insuficiente")
                .hasMessageContaining("100")
                .hasMessageContaining("101");
        }

        @Test
        @DisplayName("Deve lançar exceção quando quantidade for zero")
        void deveLancarExcecaoQuantidadeZero() {
            assertThatThrownBy(() -> estoque.remover(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maior que zero");
        }
    }

    // =========================================================================
    // RESERVAR / LIBERAR
    // =========================================================================

    @Nested
    @DisplayName("Reservar e Liberar estoque")
    class ReservarLiberarEstoque {

        @Test
        @DisplayName("Deve reservar quantidade (equivale a remover)")
        void deveReservarQuantidade() {
            estoque.reservar(20);
            assertThat(estoque.getQuantidade()).isEqualTo(80);
        }

        @Test
        @DisplayName("Deve liberar quantidade (equivale a adicionar)")
        void deveLiberarQuantidade() {
            estoque.liberar(20);
            assertThat(estoque.getQuantidade()).isEqualTo(120);
        }

        @Test
        @DisplayName("Deve lançar exceção ao reservar mais que disponível")
        void deveLancarExcecaoReservarMaisQueDisponivel() {
            assertThatThrownBy(() -> estoque.reservar(200))
                .isInstanceOf(IllegalStateException.class);
        }
    }

    // =========================================================================
    // AJUSTAR
    // =========================================================================

    @Nested
    @DisplayName("Ajustar estoque")
    class AjustarEstoque {

        @Test
        @DisplayName("Deve ajustar para nova quantidade")
        void deveAjustarParaNovaQuantidade() {
            estoque.ajustar(500);
            assertThat(estoque.getQuantidade()).isEqualTo(500);
        }

        @Test
        @DisplayName("Deve ajustar para zero")
        void deveAjustarParaZero() {
            estoque.ajustar(0);
            assertThat(estoque.getQuantidade()).isEqualTo(0);
        }

        @Test
        @DisplayName("Deve lançar exceção quando quantidade ajustada for negativa")
        void deveLancarExcecaoQuantidadeNegativa() {
            assertThatThrownBy(() -> estoque.ajustar(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negativa");
        }

        @Test
        @DisplayName("Deve lançar exceção quando nova quantidade for nula")
        void deveLancarExcecaoQuantidadeNula() {
            assertThatThrownBy(() -> estoque.ajustar(null))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    @Nested
    @DisplayName("Helpers de verificação")
    class HelpersVerificacao {

        @Test
        @DisplayName("temEstoqueSuficiente deve retornar true quando quantidade disponível")
        void temEstoqueSuficienteTrue() {
            assertThat(estoque.temEstoqueSuficiente(100)).isTrue();
            assertThat(estoque.temEstoqueSuficiente(50)).isTrue();
        }

        @Test
        @DisplayName("temEstoqueSuficiente deve retornar false quando insuficiente")
        void temEstoqueSuficienteFalse() {
            assertThat(estoque.temEstoqueSuficiente(101)).isFalse();
        }

        @Test
        @DisplayName("isEstoqueBaixo deve retornar true quando abaixo do limite")
        void isEstoqueBaixoTrue() {
            assertThat(estoque.isEstoqueBaixo(100)).isTrue();
            assertThat(estoque.isEstoqueBaixo(150)).isTrue();
        }

        @Test
        @DisplayName("isEstoqueBaixo deve retornar false quando acima do limite")
        void isEstoqueBaixoFalse() {
            assertThat(estoque.isEstoqueBaixo(50)).isFalse();
        }

        @Test
        @DisplayName("isSemEstoque deve retornar true quando quantidade for zero")
        void isSemEstoqueTrue() {
            estoque.ajustar(0);
            assertThat(estoque.isSemEstoque()).isTrue();
        }

        @Test
        @DisplayName("isSemEstoque deve retornar false quando há estoque")
        void isSemEstoqueFalse() {
            assertThat(estoque.isSemEstoque()).isFalse();
        }
    }
}
