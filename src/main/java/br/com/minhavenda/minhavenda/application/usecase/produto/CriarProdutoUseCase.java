package br.com.minhavenda.minhavenda.application.usecase.produto;

import br.com.minhavenda.minhavenda.application.dto.ProdutoDTO;
import br.com.minhavenda.minhavenda.application.mapper.ProdutoMapper;
import br.com.minhavenda.minhavenda.domain.entity.Estoque;
import br.com.minhavenda.minhavenda.domain.entity.Produto;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.ProdutoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CriarProdutoUseCase {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    @Transactional
    public ProdutoDTO execute(ProdutoDTO produtoDTO) {
        // Mapeia o DTO para a entidade
        Produto produto = produtoMapper.toEntity(produtoDTO);
        
        // Cria o registro de estoque inicial se necessário
        if (produtoDTO.getQuantidadeEstoque() != null && produtoDTO.getQuantidadeEstoque() > 0) {
            Estoque estoque = Estoque.builder()
                .produto(produto)
                .quantidade(produtoDTO.getQuantidadeEstoque())
                .build();
            produto.adicionarEstoque(produtoDTO.getQuantidadeEstoque());
        }
        
        // Salva o produto no banco de dados (o estoque será salvo em cascata)
        Produto produtoSalvo = produtoRepository.save(produto);
        
        // Converte a entidade salva de volta para DTO e retorna
        return produtoMapper.toDTO(produtoSalvo);
    }
}
