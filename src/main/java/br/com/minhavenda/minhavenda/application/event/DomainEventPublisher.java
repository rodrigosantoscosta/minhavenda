package br.com.minhavenda.minhavenda.application.event;


import br.com.minhavenda.minhavenda.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Publica domain events para o ApplicationEventPublisher do Spring
 * Os eventos serão processados de forma assíncrona pelos listeners
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Publica um único evento
     */
    public void publish(DomainEvent event) {
        log.info("Publicando evento: {} [ID: {}]",
                event.getEventType(),
                event.getEventId());

        applicationEventPublisher.publishEvent(event);

        log.debug("Evento publicado: {}", event.getEventId());
    }

    /**
     * Publica múltiplos eventos de uma só vez
     */
    public void publishAll(List<DomainEvent> events) {
        log.info("Publicando {} eventos", events.size());

        events.forEach(this::publish);

        log.debug("Todos os eventos publicados");
    }
}
