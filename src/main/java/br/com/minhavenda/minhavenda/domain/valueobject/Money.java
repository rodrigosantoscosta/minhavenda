package br.com.minhavenda.minhavenda.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Money {

    @Column(precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(length = 3)
    private String moeda = "BRL";


    public static Money of(BigDecimal valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Valor não pode ser nulo");
        }
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor não pode ser negativo");
        }
        return new Money(valor.setScale(2, RoundingMode.HALF_UP), "BRL");
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO, "BRL");
    }

    public Money somar(Money outro) {
        validarMoeda(outro);
        return new Money(this.valor.add(outro.valor), this.moeda);
    }

    public Money subtrair(Money outro) {
        validarMoeda(outro);
        BigDecimal resultado = this.valor.subtract(outro.valor);
        if (resultado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Resultado não pode ser negativo");
        }
        return new Money(resultado, this.moeda);
    }

    public Money multiplicar(int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("Quantidade não pode ser negativa");
        }
        return new Money(
                this.valor.multiply(BigDecimal.valueOf(quantidade)),
                this.moeda);
    }

    public boolean maiorQue(Money outro) {
        validarMoeda(outro);
        return this.valor.compareTo(outro.valor) > 0;
    }

    public boolean menorQue(Money outro) {
        validarMoeda(outro);
        return this.valor.compareTo(outro.valor) < 0;
    }

    private void validarMoeda(Money outro) {
        if (!this.moeda.equals(outro.moeda)) {
            throw new IllegalArgumentException("Não é possível operar com moedas diferentes");
        }
    }

    @Override
    public String toString() {
        return String.format("%s %.2f", moeda, valor);
    }

}
