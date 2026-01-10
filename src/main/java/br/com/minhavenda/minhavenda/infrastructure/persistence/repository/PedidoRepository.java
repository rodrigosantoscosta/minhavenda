package br.com.minhavenda.minhavenda.infrastructure.persistence.repository;

import br.com.minhavenda.minhavenda.domain.entity.Pedido;
import br.com.minhavenda.minhavenda.domain.entity.Usuario;
import br.com.minhavenda.minhavenda.domain.enums.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    List<Pedido> findByUsuarioOrderByDataCriacaoDesc(Usuario usuario);

    Optional<Pedido> findByIdAndUsuario(UUID id, Usuario usuario);

    List<Pedido> findByStatus(StatusPedido status);
}
