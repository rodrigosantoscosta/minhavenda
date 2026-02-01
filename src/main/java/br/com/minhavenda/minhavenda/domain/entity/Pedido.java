package br.com.minhavenda.minhavenda.domain.entity;

import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoCriadoEvent;
import br.com.minhavenda.minhavenda.domain.event.pedido.PedidoEnviadoEvent;
import br.com.minhavenda.minhavenda.domain.enums.StatusPedido;
import br.com.minhavenda.minhavenda.domain.event.DomainEvent;
import br.com.minhavenda.minhavenda.domain.event.pedido.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "pedidos")
@Getter
@NoArgsConstructor
public class Pedido{
    
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
    public Pedido(Usuario usuario, BigDecimal subtotal, BigDecimal valorFrete,
                  BigDecimal valorDesconto, BigDecimal valorTotal, 
                  String enderecoEntrega, String observacoes) {
        this.usuario = usuario;
        this.subtotal = subtotal;
        this.valorFrete = valorFrete;
        this.valorDesconto = valorDesconto;
        this.valorTotal = valorTotal;
        this.enderecoEntrega = enderecoEntrega;
        this.observacoes = observacoes;
        this.status = StatusPedido.CRIADO;
        this.dataCriacao = Instant.now();
        this.dataAtualizacao = Instant.now();
        
        //  EMITIR EVENTO: Pedido Criado
        this.registrarEvento(new PedidoCriadoEvent(
            this.id,
            usuario.getId(),
            usuario.getEmail(),
            usuario.getNome(),
            valorTotal.doubleValue(),
            0 // será atualizado depois ao adicionar itens
        ));
    }
    
    // ========================================================================
    // MÉTODOS DE NEGÓCIO (com eventos)
    // ========================================================================
    
    /**
     * Pagar o pedido
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
        
        //  EMITIR EVENTO: Pedido Pago
        this.registrarEvento(new PedidoPagoEvent(
            this.id,
            this.usuario.getId(),
            this.usuario.getEmail(),
            this.valorTotal.doubleValue(),
            metodoPagamento
        ));
    }
    
    /**
     * Marcar como enviado
     */
    public void enviar(String codigoRastreio, String transportadora, String telefone) {
        // Validação
        if (this.status != StatusPedido.PAGO) {
            throw new IllegalStateException(
                "Apenas pedidos pagos podem ser enviados. Status atual: " + this.status
            );
        }
        
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
        
        //  EMITIR EVENTO: Pedido Enviado
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
     * Cancelar pedido
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
        
        //  EMITIR EVENTO: Pedido Cancelado
        this.registrarEvento(new PedidoCanceladoEvent(
            this.id,
            this.usuario.getId(),
            this.usuario.getEmail(),
            motivo
        ));
    }
    
    /**
     * Marcar como entregue
     */
    public void marcarComoEntregue() {
        // Validação
        if (this.status != StatusPedido.ENVIADO) {
            throw new IllegalStateException(
                "Apenas pedidos enviados podem ser marcados como entregues"
            );
        }
        
        // Mudança de estado
        this.status = StatusPedido.ENTREGUE;
        this.dataEntrega = Instant.now();
        this.dataAtualizacao = Instant.now();
        
        // TODO: Criar PedidoEntregueEvent se quiser notificar
    }
    
    /**
     * Adicionar item ao pedido
     */
    public void adicionarItem(Produto produto, Integer quantidade, BigDecimal precoUnitario) {
        ItemPedido item = new ItemPedido(this, produto, quantidade, precoUnitario);
        this.itens.add(item);
        this.dataAtualizacao = Instant.now();
    }
    
    // ========================================================================
    // GERENCIAMENTO DE EVENTOS
    // ========================================================================
    
    private void registrarEvento(DomainEvent evento) {
        this.domainEvents.add(evento);
    }
    
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(this.domainEvents);
    }
    
    public void limparEventos() {
        this.domainEvents.clear();
    }
    
    // ========================================================================
    // HELPERS
    // ========================================================================
    
    public Integer getQuantidadeItens() {
        return itens.stream()
            .mapToInt(ItemPedido::getQuantidade)
            .sum();
    }
}
