package br.com.minhavenda.minhavenda.application.mapper;

import br.com.minhavenda.minhavenda.application.dto.pedido.*;
import br.com.minhavenda.minhavenda.domain.entity.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PedidoMapper {

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
                .dataCriacao(pedido.getDataCriacao())
                .dataPagamento(pedido.getDataPagamento())
                .dataEnvio(pedido.getDataEnvio())
                .dataEntrega(pedido.getDataEntrega())
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
                .dataCriacao(pedido.getDataCriacao())
                .dataPagamento(pedido.getDataPagamento())
                .dataEnvio(pedido.getDataEnvio())
                .dataEntrega(pedido.getDataEntrega())
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
