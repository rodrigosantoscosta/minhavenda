package br.com.minhavenda.minhavenda.domain.exception;

/**
 * Exceção lançada para erros de negócio
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}