package br.com.minhavenda.minhavenda.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários do Value Object Money.
 *
 * Cobre:
 * - Criação e validação
 * - Operações aritméticas (somar, subtrair, multiplicar)
 * - Comparações (maiorQue, menorQue)
 * - toString
 */
@DisplayName("Money - Testes Unitários")
class MoneyTest {

    // =========================================================================
    // CRIAÇÃO
    // =========================================================================

    @Nested
    @DisplayName("Criação")
    class Criacao {

        @Test
        @DisplayName("Deve criar Money com valor positivo")
        void deveCriarComValorPositivo() {
            Money money = Money.of(BigDecimal.valueOf(100.50));
            assertThat(money.getValor()).isEqualByComparingTo(BigDecimal.valueOf(100.50));
            assertThat(money.getMoeda()).isEqualTo("BRL");
        }

        @Test
        @DisplayName("Deve criar Money com valor zero")
        void deveCriarComValorZero() {
            Money money = Money.of(BigDecimal.ZERO);
            assertThat(money.getValor()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Deve criar Money zero com factory zero()")
        void deveCriarZeroComFactory() {
            Money zero = Money.zero();
            assertThat(zero.getValor()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Deve arredondar para 2 casas decimais")
        void deveArredondarParaDuasCasas() {
            Money money = Money.of(BigDecimal.valueOf(10.999));
            assertThat(money.getValor()).isEqualByComparingTo(BigDecimal.valueOf(11.00));
        }

        @Test
        @DisplayName("Deve lançar exceção quando valor for nulo")
        void deveLancarExcecaoValorNulo() {
            assertThatThrownBy(() -> Money.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção quando valor for negativo")
        void deveLancarExcecaoValorNegativo() {
            assertThatThrownBy(() -> Money.of(BigDecimal.valueOf(-1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negativo");
        }
    }

    // =========================================================================
    // OPERAÇÕES ARITMÉTICAS
    // =========================================================================

    @Nested
    @DisplayName("Operações aritméticas")
    class OperacoesAritmeticas {

        @Test
        @DisplayName("Deve somar dois valores corretamente")
        void deveSomarDoisValores() {
            Money a = Money.of(BigDecimal.valueOf(100));
            Money b = Money.of(BigDecimal.valueOf(50));

            Money resultado = a.somar(b);

            assertThat(resultado.getValor()).isEqualByComparingTo(BigDecimal.valueOf(150));
        }

        @Test
        @DisplayName("Deve subtrair dois valores corretamente")
        void deveSubtrairDoisValores() {
            Money a = Money.of(BigDecimal.valueOf(100));
            Money b = Money.of(BigDecimal.valueOf(30));

            Money resultado = a.subtrair(b);

            assertThat(resultado.getValor()).isEqualByComparingTo(BigDecimal.valueOf(70));
        }

        @Test
        @DisplayName("Deve lançar exceção ao subtrair resultando negativo")
        void deveLancarExcecaoSubtracaoNegativa() {
            Money a = Money.of(BigDecimal.valueOf(10));
            Money b = Money.of(BigDecimal.valueOf(50));

            assertThatThrownBy(() -> a.subtrair(b))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negativo");
        }

        @Test
        @DisplayName("Deve multiplicar por quantidade corretamente")
        void deveMultiplicarPorQuantidade() {
            Money money = Money.of(BigDecimal.valueOf(25));

            Money resultado = money.multiplicar(4);

            assertThat(resultado.getValor()).isEqualByComparingTo(BigDecimal.valueOf(100));
        }

        @Test
        @DisplayName("Deve lançar exceção ao multiplicar por quantidade negativa")
        void deveLancarExcecaoMultiplicacaoNegativa() {
            Money money = Money.of(BigDecimal.valueOf(25));

            assertThatThrownBy(() -> money.multiplicar(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negativa");
        }
    }

    // =========================================================================
    // COMPARAÇÕES
    // =========================================================================

    @Nested
    @DisplayName("Comparações")
    class Comparacoes {

        @Test
        @DisplayName("maiorQue deve retornar true quando maior")
        void maiorQueTrue() {
            Money cem = Money.of(BigDecimal.valueOf(100));
            Money cinquenta = Money.of(BigDecimal.valueOf(50));
            assertThat(cem.maiorQue(cinquenta)).isTrue();
        }

        @Test
        @DisplayName("maiorQue deve retornar false quando menor")
        void maiorQueFalse() {
            Money cinquenta = Money.of(BigDecimal.valueOf(50));
            Money cem = Money.of(BigDecimal.valueOf(100));
            assertThat(cinquenta.maiorQue(cem)).isFalse();
        }

        @Test
        @DisplayName("menorQue deve retornar true quando menor")
        void menorQueTrue() {
            Money cinquenta = Money.of(BigDecimal.valueOf(50));
            Money cem = Money.of(BigDecimal.valueOf(100));
            assertThat(cinquenta.menorQue(cem)).isTrue();
        }
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    @DisplayName("toString deve formatar corretamente como 'BRL X.XX'")
    void deveFormatarToStringCorretamente() {
        Money money = Money.of(BigDecimal.valueOf(99.9));
        assertThat(money.toString()).isEqualTo("BRL 99.90");
    }
}
