package br.com.minhavenda.minhavenda.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários do Value Object Email.
 *
 * Cobre:
 * - Emails válidos
 * - Emails inválidos (formato, nulo, vazio)
 * - Normalização (lowercase, trim)
 * - equals e hashCode
 */
@DisplayName("Email - Testes Unitários")
class EmailTest {

    // =========================================================================
    // EMAILS VÁLIDOS
    // =========================================================================

    @Nested
    @DisplayName("Emails válidos")
    class EmailsValidos {

        @ParameterizedTest
        @DisplayName("Deve aceitar formatos de email válidos")
        @ValueSource(strings = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.com.br",
            "USER@EXAMPLE.COM",
            "  user@example.com  "  // Com espaços — deve normalizar
        })
        void deveAceitarEmailsValidos(String emailValido) {
            Email email = Email.of(emailValido);
            assertThat(email).isNotNull();
            assertThat(email.getValor()).isNotBlank();
        }

        @Test
        @DisplayName("Deve normalizar email para lowercase e sem espaços")
        void deveNormalizarEmail() {
            Email email = Email.of("  USUARIO@TESTE.COM  ");
            assertThat(email.getValor()).isEqualTo("usuario@teste.com");
        }
    }

    // =========================================================================
    // EMAILS INVÁLIDOS
    // =========================================================================

    @Nested
    @DisplayName("Emails inválidos")
    class EmailsInvalidos {

        @Test
        @DisplayName("Deve lançar exceção quando email for nulo")
        void deveLancarExcecaoEmailNulo() {
            assertThatThrownBy(() -> Email.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção quando email for vazio")
        void deveLancarExcecaoEmailVazio() {
            assertThatThrownBy(() -> Email.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vazio");
        }

        @ParameterizedTest
        @DisplayName("Deve lançar exceção para formatos inválidos")
        @ValueSource(strings = {
            "naotemdominio",
            "@semlocal.com",
            "sem@ponto",
            "duplo@@dominio.com",
            "espaço@dominio.com"
        })
        void deveLancarExcecaoEmailInvalido(String emailInvalido) {
            assertThatThrownBy(() -> Email.of(emailInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inválido");
        }
    }

    // =========================================================================
    // EQUALS / HASHCODE
    // =========================================================================

    @Nested
    @DisplayName("Igualdade")
    class Igualdade {

        @Test
        @DisplayName("Dois emails com mesmo valor devem ser iguais")
        void doisEmailsIguaisSaoIguais() {
            Email email1 = Email.of("user@test.com");
            Email email2 = Email.of("USER@TEST.COM");  // normalizado para mesmo valor

            assertThat(email1).isEqualTo(email2);
            assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        }

        @Test
        @DisplayName("Emails diferentes não devem ser iguais")
        void emailsDiferentesNaoSaoIguais() {
            Email email1 = Email.of("a@test.com");
            Email email2 = Email.of("b@test.com");

            assertThat(email1).isNotEqualTo(email2);
        }
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    @DisplayName("toString deve retornar o valor do email")
    void toStringDeveRetornarValor() {
        Email email = Email.of("user@test.com");
        assertThat(email.toString()).isEqualTo("user@test.com");
    }
}
