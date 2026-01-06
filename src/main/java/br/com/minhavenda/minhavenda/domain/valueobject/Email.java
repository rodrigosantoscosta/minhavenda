package br.com.minhavenda.minhavenda.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.regex.Pattern;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Email {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @Column(name = "email", length = 254)
    private String valor;


    public static Email of(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email não pode ser vazio");
        }

        String emailNormalizado = email.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(emailNormalizado).matches()) {
            throw new IllegalArgumentException("Email inválido: " + email);
        }

        return new Email(emailNormalizado);
    }

    @Override
    public String toString() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return valor.equals(email.valor);
    }

    @Override
    public int hashCode() {
        return valor.hashCode();
    }
}