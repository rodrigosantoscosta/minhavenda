package br.com.minhavenda.minhavenda.domain.exception;

/**
 * Exceção lançada quando uma entidade já existe
 */
public class EntityAlreadyExistsException extends RuntimeException {
    public EntityAlreadyExistsException(String message) {
        super(message);
    }

    public EntityAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}