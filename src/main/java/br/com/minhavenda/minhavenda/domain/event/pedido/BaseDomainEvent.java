package br.com.minhavenda.minhavenda.domain.event.pedido;

import br.com.minhavenda.minhavenda.domain.event.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class BaseDomainEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredOn;

    protected BaseDomainEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
    }

    @Override
    public String getEventType() {
        return this.getClass().getSimpleName();
    }
}