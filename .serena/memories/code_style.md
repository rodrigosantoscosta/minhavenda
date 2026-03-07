# Estilo e Convenções - MinhaVenda

## Nomenclatura Java
- Classes: PascalCase (`PedidoCriadoProducer`)
- Métodos: camelCase (`publicarPedidoCriado`)
- Constantes: UPPER_SNAKE_CASE (`PEDIDO_CRIADO_QUEUE`)
- Packages: lowercase

## Padrões Use Case
- Anotações: `@Service @RequiredArgsConstructor @Slf4j`
- Método principal: `executar(...)`
- `@Transactional` nos use cases, NÃO nos controllers

## Padrões Entity
- `@Entity @Table @Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)`
- Datas: `Instant` nas entities, `LocalDateTime` nos DTOs
- Emitir domain events dentro dos métodos de negócio

## Padrões Evento
1. Entidade emite evento via `registrarEvento()`
2. Use Case publica com `eventPublisher.publishAll()`
3. Limpar com `limparEventos()`

## Testes
- Nomes: `deve{Ação}{Esperado}` ex: `devePubilicarMensagemNoRabbitMQ`
- `@SpringBootTest` para integração, `@MockBean` para externos
