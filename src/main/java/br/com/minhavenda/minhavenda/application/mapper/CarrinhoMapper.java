package br.com.minhavenda.minhavenda.application.mapper;

import br.com.minhavenda.minhavenda.application.dto.carrinho.CarrinhoDTO;
import br.com.minhavenda.minhavenda.application.dto.carrinho.ItemCarrinhoDTO;
import br.com.minhavenda.minhavenda.domain.entity.Carrinho;
import br.com.minhavenda.minhavenda.domain.entity.ItemCarrinho;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre Carrinho/ItemCarrinho e DTOs.
 */
@Component
public class CarrinhoMapper {

    /**
     * Converte Carrinho entity para DTO.
     * 
     * @param carrinho entity
     * @return DTO completo com itens
     */
    public CarrinhoDTO toDTO(Carrinho carrinho) {
        if (carrinho == null) {
            return null;
        }
        
        List<ItemCarrinhoDTO> itensDTO = carrinho.getItens().stream()
                .map(this::itemToDTO)
                .collect(Collectors.toList());
        
        Integer quantidadeTotal = carrinho.getItens().stream()
                .mapToInt(ItemCarrinho::getQuantidade)
                .sum();
        
        return CarrinhoDTO.builder()
                .id(carrinho.getId())
                .usuarioId(carrinho.getUsuario().getId())
                .status(carrinho.getStatus())
                .itens(itensDTO)
                .valorTotal(carrinho.getValorTotal())
                .quantidadeTotal(quantidadeTotal)
                .dataCriacao(carrinho.getDataCriacao())
                .dataAtualizacao(carrinho.getDataAtualizacao())
                .build();
    }

    /**
     * Converte ItemCarrinho entity para DTO.
     * 
     * @param item entity
     * @return DTO com dados do item e produto
     */
    public ItemCarrinhoDTO itemToDTO(ItemCarrinho item) {
        if (item == null) {
            return null;
        }
        
        return ItemCarrinhoDTO.builder()
                .id(item.getId())
                .produtoId(item.getProduto().getId())
                .produtoNome(item.getProduto().getNome())
                .produtoDescricao(item.getProduto().getDescricao())
                .quantidade(item.getQuantidade())
                .precoUnitario(item.getPrecoUnitario())
                .subtotal(item.getSubtotal())
                .build();
    }
}
