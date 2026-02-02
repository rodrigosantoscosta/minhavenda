package br.com.minhavenda.minhavenda.application.mapper;

import br.com.minhavenda.minhavenda.application.dto.pedido.*;
import br.com.minhavenda.minhavenda.domain.entity.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;

@Component
public class PedidoMapper {

    @Value("${app.timezone:America/Sao_Paulo}")
    private String timeZone;

    private ZoneId getZoneId() {
        return ZoneId.of(timeZone);
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, getZoneId());
    }

    public PedidoDTO toDTO(Pedido pedido) {
        return PedidoDTO.builder()
                .id(pedido.getId())
                .status(pedido.getStatus())
                .subtotal(pedido.getSubtotal())
                .valorFrete(pedido.getValorFrete())
                .valorDesconto(pedido.getValorDesconto())
                .valorTotal(pedido.getValorTotal())
                .enderecoEntrega(pedido.getEnderecoEntrega())
                .observacoes(pedido.getObservacoes())
                .quantidadeItens(pedido.getQuantidadeTotal())

                .dataCriacao(toLocalDateTime(pedido.getDataEnvio()))
                .dataPagamento(toLocalDateTime(pedido.getDataEnvio()))
                .dataEnvio(toLocalDateTime(pedido.getDataEnvio()))
                .dataEntrega(toLocalDateTime(pedido.getDataEnvio()))

                .build();
    }

    public PedidoDetalhadoDTO toDetalhadoDTO(Pedido pedido) {
        return PedidoDetalhadoDTO.builder()
                .id(pedido.getId())
                .status(pedido.getStatus())
                .subtotal(pedido.getSubtotal())
                .valorFrete(pedido.getValorFrete())
                .valorDesconto(pedido.getValorDesconto())
                .valorTotal(pedido.getValorTotal())
                .enderecoEntrega(pedido.getEnderecoEntrega())
                .observacoes(pedido.getObservacoes())
                .quantidadeItens(pedido.getQuantidadeTotal())

                .dataCriacao(toLocalDateTime(pedido.getDataCriacao()))
                .dataPagamento(toLocalDateTime(pedido.getDataPagamento()))
                .dataEnvio(toLocalDateTime(pedido.getDataEnvio()))
                .dataEntrega(toLocalDateTime(pedido.getDataEntrega()))
                .dataPagamento(toLocalDateTime(pedido.getDataPagamento()))
                .dataEnvio(toLocalDateTime(pedido.getDataEnvio()))
                .dataEntrega(toLocalDateTime(pedido.getDataEnvio()))

                .itens(pedido.getItens().stream()
                        .map(this::itemToDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    public ItemPedidoDTO itemToDTO(ItemPedido item) {
        return ItemPedidoDTO.builder()
                .id(item.getId())
                .produtoId(item.getProduto().getId())
                .produtoNome(item.getProdutoNome())
                .quantidade(item.getQuantidade())
                .precoUnitario(item.getPrecoUnitario())
                .subtotal(item.getSubtotal())
                .build();
    }
}
