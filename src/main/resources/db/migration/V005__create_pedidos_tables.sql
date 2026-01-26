-- V005__create_pedidos_tables.sql
-- Migration para criar tabelas de pedidos

-- Tabela de Pedidos
CREATE TABLE IF NOT EXISTS pedidos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CRIADO',
    subtotal NUMERIC(10, 2) NOT NULL DEFAULT 0,
    valor_frete NUMERIC(10, 2) NOT NULL DEFAULT 0,
    valor_desconto NUMERIC(10, 2) NOT NULL DEFAULT 0,
    valor_total NUMERIC(10, 2) NOT NULL DEFAULT 0,
    endereco_entrega VARCHAR(500) NOT NULL,
    observacoes VARCHAR(1000),
    quantidade_itens INTEGER NOT NULL DEFAULT 0,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_pagamento TIMESTAMP,
    data_envio TIMESTAMP,
    data_entrega TIMESTAMP,
    
    CONSTRAINT pedidos_status_check CHECK (status IN ('CRIADO', 'PAGO', 'ENVIADO', 'ENTREGUE', 'CANCELADO')),
    CONSTRAINT pedidos_subtotal_check CHECK (subtotal >= 0),
    CONSTRAINT pedidos_frete_check CHECK (valor_frete >= 0),
    CONSTRAINT pedidos_desconto_check CHECK (valor_desconto >= 0),
    CONSTRAINT pedidos_total_check CHECK (valor_total >= 0),
    CONSTRAINT pedidos_quantidade_check CHECK (quantidade_itens > 0),
    CONSTRAINT fk_pedidos_usuario FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Tabela de Itens do Pedido
CREATE TABLE IF NOT EXISTS itens_pedido (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id UUID NOT NULL,
    produto_id UUID NOT NULL,
    produto_nome VARCHAR(100) NOT NULL,
    quantidade INTEGER NOT NULL,
    preco_unitario NUMERIC(10, 2) NOT NULL,
    subtotal NUMERIC(10, 2) NOT NULL,
    
    CONSTRAINT itens_pedido_quantidade_check CHECK (quantidade > 0),
    CONSTRAINT itens_pedido_preco_check CHECK (preco_unitario >= 0),
    CONSTRAINT itens_pedido_subtotal_check CHECK (subtotal >= 0),
    CONSTRAINT fk_itens_pedido_pedido FOREIGN KEY (pedido_id) 
        REFERENCES pedidos(id) ON DELETE CASCADE,
    CONSTRAINT fk_itens_pedido_produto FOREIGN KEY (produto_id) 
        REFERENCES produtos(id) ON DELETE RESTRICT
);

-- Índices para Pedidos
CREATE INDEX idx_pedidos_usuario ON pedidos(usuario_id);
CREATE INDEX idx_pedidos_status ON pedidos(status);
CREATE INDEX idx_pedidos_data_criacao ON pedidos(data_criacao DESC);
CREATE INDEX idx_pedidos_data_pagamento ON pedidos(data_pagamento DESC);
CREATE INDEX idx_pedidos_data_envio ON pedidos(data_envio DESC);
CREATE INDEX idx_pedidos_usuario_status ON pedidos(usuario_id, status);

-- Índices para Itens do Pedido
CREATE INDEX idx_itens_pedido_pedido ON itens_pedido(pedido_id);
CREATE INDEX idx_itens_pedido_produto ON itens_pedido(produto_id);

-- Comentários - Pedidos
COMMENT ON TABLE pedidos IS 'Tabela de pedidos/vendas realizadas';
COMMENT ON COLUMN pedidos.id IS 'Identificador único do pedido (UUID)';
COMMENT ON COLUMN pedidos.usuario_id IS 'Referência ao usuário que fez o pedido';
COMMENT ON COLUMN pedidos.status IS 'Status do pedido: CRIADO, PAGO, ENVIADO, ENTREGUE, CANCELADO';
COMMENT ON COLUMN pedidos.subtotal IS 'Soma dos valores dos itens';
COMMENT ON COLUMN pedidos.valor_frete IS 'Valor do frete';
COMMENT ON COLUMN pedidos.valor_desconto IS 'Valor de desconto aplicado';
COMMENT ON COLUMN pedidos.valor_total IS 'Valor final: subtotal + frete - desconto';
COMMENT ON COLUMN pedidos.endereco_entrega IS 'Endereço completo de entrega';
COMMENT ON COLUMN pedidos.data_pagamento IS 'Data/hora em que o pedido foi pago';
COMMENT ON COLUMN pedidos.data_envio IS 'Data/hora em que o pedido foi enviado';
COMMENT ON COLUMN pedidos.data_entrega IS 'Data/hora em que o pedido foi entregue';

-- Comentários - Itens do Pedido
COMMENT ON TABLE itens_pedido IS 'Tabela de itens dentro dos pedidos';
COMMENT ON COLUMN itens_pedido.id IS 'Identificador único do item (UUID)';
COMMENT ON COLUMN itens_pedido.pedido_id IS 'Referência ao pedido';
COMMENT ON COLUMN itens_pedido.produto_id IS 'Referência ao produto';
COMMENT ON COLUMN itens_pedido.produto_nome IS 'Nome do produto (snapshot)';
COMMENT ON COLUMN itens_pedido.preco_unitario IS 'Preço do produto no momento da compra';
COMMENT ON COLUMN itens_pedido.subtotal IS 'Preço unitário × quantidade';

-- Trigger para validar datas do pedido
CREATE OR REPLACE FUNCTION validar_datas_pedido()
RETURNS TRIGGER AS $$
BEGIN
    -- Data de pagamento deve ser após criação
    IF NEW.data_pagamento IS NOT NULL AND NEW.data_pagamento < NEW.data_criacao THEN
        RAISE EXCEPTION 'Data de pagamento não pode ser anterior à data de criação';
    END IF;
    
    -- Data de envio deve ser após pagamento
    IF NEW.data_envio IS NOT NULL AND (NEW.data_pagamento IS NULL OR NEW.data_envio < NEW.data_pagamento) THEN
        RAISE EXCEPTION 'Data de envio deve ser após o pagamento';
    END IF;
    
    -- Data de entrega deve ser após envio
    IF NEW.data_entrega IS NOT NULL AND (NEW.data_envio IS NULL OR NEW.data_entrega < NEW.data_envio) THEN
        RAISE EXCEPTION 'Data de entrega deve ser após o envio';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_validar_datas_pedido
    BEFORE INSERT OR UPDATE ON pedidos
    FOR EACH ROW
    EXECUTE FUNCTION validar_datas_pedido();

-- Trigger para validar transições de status
CREATE OR REPLACE FUNCTION validar_transicao_status_pedido()
RETURNS TRIGGER AS $$
BEGIN
    -- Pedido cancelado não pode mudar de status
    IF OLD.status = 'CANCELADO' AND NEW.status != 'CANCELADO' THEN
        RAISE EXCEPTION 'Pedido cancelado não pode ter status alterado';
    END IF;
    
    -- Pedido entregue não pode mudar de status
    IF OLD.status = 'ENTREGUE' AND NEW.status != 'ENTREGUE' THEN
        RAISE EXCEPTION 'Pedido entregue não pode ter status alterado';
    END IF;
    
    -- Atualizar datas baseado no novo status
    IF NEW.status = 'PAGO' AND OLD.status != 'PAGO' THEN
        NEW.data_pagamento = CURRENT_TIMESTAMP;
    END IF;
    
    IF NEW.status = 'ENVIADO' AND OLD.status != 'ENVIADO' THEN
        NEW.data_envio = CURRENT_TIMESTAMP;
    END IF;
    
    IF NEW.status = 'ENTREGUE' AND OLD.status != 'ENTREGUE' THEN
        NEW.data_entrega = CURRENT_TIMESTAMP;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_validar_transicao_status
    BEFORE UPDATE ON pedidos
    FOR EACH ROW
    WHEN (OLD.status IS DISTINCT FROM NEW.status)
    EXECUTE FUNCTION validar_transicao_status_pedido();
