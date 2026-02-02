package br.com.minhavenda.minhavenda.domain.entity;

import br.com.minhavenda.minhavenda.domain.event.pedido.*;
import br.com.minhavenda.minhavenda.domain.enums.StatusPedido;
import br.com.minhavenda.minhavenda.domain.event.DomainEvent;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Aggregate Root: Pedido
 *
 * Representa um pedido realizado por um usuário.
 * Gerencia seu próprio ciclo de vida e emite domain events.
 *
 * Fluxo de status:
 * CRIADO → PAGO → ENVIADO → ENTREGUE
 *    ↓        ↓
 * CANCELADO  CANCELADO
 */
@Entity
@Table(name = "pedidos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "valor_frete", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorFrete;

    @Column(name = "valor_desconto", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDesconto;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "quantidade_itens", nullable = false)
    private Integer quantidadeItens = 0;

    @Column(name = "endereco_entrega", nullable = false, length = 500)
    private String enderecoEntrega;

    @Column(length = 1000)
    private String observacoes;

    @Column(name = "codigo_rastreio", length = 100)
    private String codigoRastreio;

    @Column(length = 100)
    private String transportadora;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    @Column(name = "data_criacao", nullable = false)
    private Instant dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private Instant dataAtualizacao;

    @Column(name = "data_pagamento")
    private Instant dataPagamento;

    @Column(name = "data_envio")
    private Instant dataEnvio;

    @Column(name = "data_entrega")
    private Instant dataEntrega;

    // ========================================================================
    // DOMAIN EVENTS - Não são persistidos no banco
    // ========================================================================
    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // ========================================================================
    // CONSTRUTOR
    // ========================================================================

    /**
     * Cria um novo pedido com validações.
     *
     * IMPORTANTE: O evento PedidoCriadoEvent NÃO é emitido aqui porque
     * o ID ainda não foi gerado pelo banco de dados. O evento deve ser
     * emitido após a persistência usando o método registrarCriacao().
     *
     * @param usuario usuário que criou o pedido (não pode ser null)
     * @param subtotal valor dos itens (deve ser >= 0)
     * @param valorFrete valor do frete (deve ser >= 0)
     * @param valorDesconto valor do desconto (deve ser >= 0)
     * @param valorTotal valor total (deve ser >= 0)
     * @param enderecoEntrega endereço de entrega (não pode ser vazio)
     * @param observacoes observações opcionais
     * @throws IllegalArgumentException se validação falhar
     */
    public Pedido(Usuario usuario, BigDecimal subtotal, BigDecimal valorFrete,
                  BigDecimal valorDesconto, BigDecimal valorTotal,
                  String enderecoEntrega, String observacoes) {

        // Validações
        validarUsuario(usuario);
        validarValores(subtotal, valorFrete, valorDesconto, valorTotal);
        validarEnderecoEntrega(enderecoEntrega);

        // Atribuições
        this.usuario = usuario;
        this.subtotal = subtotal;
        this.valorFrete = valorFrete;
        this.valorDesconto = valorDesconto;
        this.valorTotal = valorTotal;
        this.quantidadeItens = 0;
        this.enderecoEntrega = enderecoEntrega;
        this.observacoes = observacoes;
        this.status = StatusPedido.CRIADO;
        this.dataCriacao = Instant.now();
        this.dataAtualizacao = Instant.now();
    }

    // ========================================================================
    // MÉTODOS DE NEGÓCIO (com eventos)
    // ========================================================================

    /**
     * Registra a criação do pedido e emite o evento PedidoCriadoEvent.
     *
     * Este método deve ser chamado APÓS a persistência do pedido,
     * quando o ID já foi gerado pelo banco de dados.
     *
     * Padrão de uso:
     * 1. Criar pedido com construtor
     * 2. Adicionar itens
     * 3. Salvar no banco (gera ID)
     * 4. Chamar registrarCriacao()
     * 5. Publicar eventos
     */
    public void registrarCriacao() {
        if (this.id == null) {
            throw new IllegalStateException(
                    "Pedido deve ser persistido antes de registrar criação"
            );
        }

        this.registrarEvento(new PedidoCriadoEvent(
                this.id,
                this.usuario.getId(),
                this.usuario.getEmail(),
                this.usuario.getNome(),
                this.valorTotal.doubleValue(),
                this.getQuantidadeTotal()
        ));
    }

    /**
     * Adiciona um item ao pedido.
     * Recalcula automaticamente o valor total.
     *
     * @param item item a ser adicionado
     */
    public void adicionarItem(ItemPedido item) {
        if (item == null) {
            throw new IllegalArgumentException("Item não pode ser nulo");
        }
        this.itens.add(item);
        item.setPedido(this);
        this.quantidadeItens = this.getQuantidadeTotal(); // Atualizar campo
        this.calcularValorTotal();
        this.dataAtualizacao = Instant.now();
    }

    /**
     * Adiciona um item ao pedido (sobrecarga de conveniência).
     * Cria o ItemPedido internamente.
     *
     * @param produto produto do item
     * @param quantidade quantidade
     * @param precoUnitario preço unitário no momento da compra
     */
    public void adicionarItem(Produto produto, Integer quantidade, BigDecimal precoUnitario) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo");
        }
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        if (precoUnitario == null || precoUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço unitário deve ser maior que zero");
        }

        ItemPedido item = ItemPedido.builder()
                .produto(produto)
                .produtoNome(produto.getNome())
                .quantidade(quantidade)
                .precoUnitario(precoUnitario)
                .build();

        this.adicionarItem(item);
    }

    /**
     * Marca o pedido como pago.
     * Apenas pedidos com status CRIADO podem ser pagos.
     *
     * @param metodoPagamento método de pagamento utilizado
     * @throws IllegalStateException se status atual não permite pagamento
     */
    public void pagar(String metodoPagamento) {
        // Validação
        if (this.status != StatusPedido.CRIADO) {
            throw new IllegalStateException(
                    "Apenas pedidos com status CRIADO podem ser pagos. Status atual: " + this.status
            );
        }

        // Mudança de estado
        this.status = StatusPedido.PAGO;
        this.dataPagamento = Instant.now();
        this.dataAtualizacao = Instant.now();

        // Emitir evento
        this.registrarEvento(new PedidoPagoEvent(
                this.id,
                this.usuario.getId(),
                this.usuario.getEmail(),
                this.valorTotal.doubleValue(),
                metodoPagamento
        ));
    }

    /**
     * Marca o pedido como pago (sem especificar método de pagamento).
     * Sobrecarga para compatibilidade com PedidoService.
     */
    public void marcarComoPago() {
        this.pagar("Não informado");
    }

    /**
     * Marca pedido como enviado.
     * Apenas pedidos PAGO podem ser enviados.
     *
     * @param codigoRastreio código de rastreamento (obrigatório)
     * @param transportadora nome da transportadora (obrigatório)
     * @param telefone telefone do usuário para contato
     * @throws IllegalStateException se status atual não permite envio
     * @throws IllegalArgumentException se código ou transportadora forem vazios
     */
    public void enviar(String codigoRastreio, String transportadora, String telefone) {
        // Validação de estado
        if (this.status != StatusPedido.PAGO) {
            throw new IllegalStateException(
                    "Apenas pedidos pagos podem ser enviados. Status atual: " + this.status
            );
        }

        // Validação de parâmetros
        if (codigoRastreio == null || codigoRastreio.isBlank()) {
            throw new IllegalArgumentException("Código de rastreio é obrigatório");
        }

        if (transportadora == null || transportadora.isBlank()) {
            throw new IllegalArgumentException("Transportadora é obrigatória");
        }

        // Mudança de estado
        this.status = StatusPedido.ENVIADO;
        this.codigoRastreio = codigoRastreio;
        this.transportadora = transportadora;
        this.dataEnvio = Instant.now();
        this.dataAtualizacao = Instant.now();

        // Emitir evento
        this.registrarEvento(new PedidoEnviadoEvent(
                this.id,
                this.usuario.getId(),
                this.usuario.getEmail(),
                telefone,
                codigoRastreio,
                transportadora
        ));
    }

    /**
     * Marca pedido como enviado (sobrecarga para compatibilidade com PedidoService).
     * Usa valores padrão para parâmetros não fornecidos.
     */
    public void marcarComoEnviado() {
        this.enviar("RASTREIO-PENDENTE", "Transportadora padrão", null);
    }

    /**
     * Cancela o pedido.
     * Apenas pedidos CRIADO ou PAGO podem ser cancelados.
     *
     * @param motivo motivo do cancelamento
     * @throws IllegalStateException se status atual não permite cancelamento
     */
    public void cancelar(String motivo) {
        // Validação
        if (this.status == StatusPedido.ENVIADO || this.status == StatusPedido.ENTREGUE) {
            throw new IllegalStateException(
                    "Não é possível cancelar pedidos já enviados ou entregues"
            );
        }

        if (this.status == StatusPedido.CANCELADO) {
            throw new IllegalStateException("Pedido já está cancelado");
        }

        // Mudança de estado
        this.status = StatusPedido.CANCELADO;
        this.dataAtualizacao = Instant.now();

        // Emitir evento
        this.registrarEvento(new PedidoCanceladoEvent(
                this.id,
                this.usuario.getId(),
                this.usuario.getEmail(),
                motivo
        ));
    }

    /**
     * Cancela o pedido (sobrecarga sem motivo).
     * Usa motivo padrão.
     */
    public void cancelar() {
        this.cancelar("Cancelado pelo usuário");
    }

    /**
     * Marca pedido como entregue.
     * Apenas pedidos ENVIADO podem ser marcados como entregues.
     *
     * @throws IllegalStateException se status atual não permite entrega
     */
    public void marcarComoEntregue() {
        // Validação
        if (this.status != StatusPedido.ENVIADO) {
            throw new IllegalStateException(
                    "Apenas pedidos enviados podem ser marcados como entregues. Status atual: " + this.status
            );
        }

        // Mudança de estado
        this.status = StatusPedido.ENTREGUE;
        this.dataEntrega = Instant.now();
        this.dataAtualizacao = Instant.now();

        // TODO: Criar PedidoEntregueEvent se necessário
    }

    // ========================================================================
    // GERENCIAMENTO DE EVENTOS
    // ========================================================================

    /**
     * Registra um evento de domínio.
     * O evento será publicado posteriormente pelo Use Case ou Service.
     *
     * @param evento evento a ser registrado
     */
    private void registrarEvento(DomainEvent evento) {
        this.domainEvents.add(evento);
    }

    /**
     * Retorna todos os eventos de domínio registrados.
     * Lista imutável para evitar modificações externas.
     *
     * @return lista imutável de eventos
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(this.domainEvents);
    }

    /**
     * Limpa todos os eventos registrados.
     * Deve ser chamado após a publicação dos eventos.
     */
    public void limparEventos() {
        this.domainEvents.clear();
    }

    // ========================================================================
    // CÁLCULOS E HELPERS
    // ========================================================================

    /**
     * Calcula o valor total do pedido.
     * Formula: subtotal + frete - desconto
     *
     * Este método recalcula baseado nos itens atuais.
     */
    public void calcularValorTotal() {
        // Recalcular subtotal baseado nos itens
        this.subtotal = this.itens.stream()
                .map(ItemPedido::getSubtotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular total
        this.valorTotal = this.subtotal
                .add(this.valorFrete)
                .subtract(this.valorDesconto);

        this.dataAtualizacao = Instant.now();
    }

    /**
     * Retorna a quantidade total de itens no pedido.
     *
     * @return soma das quantidades de todos os itens
     */
    public Integer getQuantidadeTotal() {
        return itens.stream()
                .mapToInt(ItemPedido::getQuantidade)
                .sum();
    }

    // ========================================================================
    // VALIDAÇÕES PRIVADAS
    // ========================================================================

    private void validarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo");
        }
    }

    private void validarValores(BigDecimal subtotal, BigDecimal valorFrete,
                                BigDecimal valorDesconto, BigDecimal valorTotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Subtotal inválido: deve ser maior ou igual a zero");
        }

        if (valorFrete == null || valorFrete.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor do frete inválido: deve ser maior ou igual a zero");
        }

        if (valorDesconto == null || valorDesconto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor do desconto inválido: deve ser maior ou igual a zero");
        }

        if (valorTotal == null || valorTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor total inválido: deve ser maior ou igual a zero");
        }
    }

    private void validarEnderecoEntrega(String enderecoEntrega) {
        if (enderecoEntrega == null || enderecoEntrega.isBlank()) {
            throw new IllegalArgumentException("Endereço de entrega é obrigatório");
        }

        if (enderecoEntrega.length() > 500) {
            throw new IllegalArgumentException("Endereço de entrega muito longo (máximo 500 caracteres)");
        }
    }
}