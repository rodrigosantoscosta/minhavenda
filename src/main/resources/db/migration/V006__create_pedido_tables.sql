-- ============================================================================
-- Migration V006: Criar tabelas de Pedido
-- ============================================================================
-- Descrição: Tabelas para gerenciar pedidos e itens de pedido
-- Data: 2026-01-16
-- IMPORTANTE: Esta migration apenas CRIA as tabelas, não altera existentes
-- ============================================================================

-- ============================================================================
-- TABELA: pedidos
-- ============================================================================
CREATE TABLE pedidos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    valor_frete DECIMAL(10,2) NOT NULL DEFAULT 0,
    valor_desconto DECIMAL(10,2) NOT NULL DEFAULT 0,
    valor_total DECIMAL(10,2) NOT NULL,
    endereco_entrega VARCHAR(500) NOT NULL,
    observacoes VARCHAR(1000),
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMP NOT NULL DEFAULT NOW(),
    data_pagamento TIMESTAMP,
    data_envio TIMESTAMP,
    data_entrega TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_pedido_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT chk_pedido_status
        CHECK (status IN ('CRIADO', 'PAGO', 'ENVIADO', 'ENTREGUE', 'CANCELADO')),
    CONSTRAINT chk_pedido_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_pedido_frete CHECK (valor_frete >= 0),
    CONSTRAINT chk_pedido_desconto CHECK (valor_desconto >= 0),
    CONSTRAINT chk_pedido_total CHECK (valor_total >= 0)
);

-- Índices para performance
CREATE INDEX idx_pedido_usuario ON pedidos(usuario_id);
CREATE INDEX idx_pedido_status ON pedidos(status);
CREATE INDEX idx_pedido_data_criacao ON pedidos(data_criacao DESC);

-- Comentários
COMMENT ON TABLE pedidos IS 'Pedidos finalizados do e-commerce';
COMMENT ON COLUMN pedidos.status IS 'Status: CRIADO, PAGO, ENVIADO, ENTREGUE, CANCELADO';
COMMENT ON COLUMN pedidos.subtotal IS 'Soma dos itens (sem frete e desconto)';
COMMENT ON COLUMN pedidos.valor_frete IS 'Valor do frete';
COMMENT ON COLUMN pedidos.valor_desconto IS 'Valor do desconto aplicado';
COMMENT ON COLUMN pedidos.valor_total IS 'Valor final (subtotal + frete - desconto)';
COMMENT ON COLUMN pedidos.data_pagamento IS 'Data em que o pedido foi pago';
COMMENT ON COLUMN pedidos.data_envio IS 'Data em que o pedido foi enviado';
COMMENT ON COLUMN pedidos.data_entrega IS 'Data em que o pedido foi entregue';

-- ============================================================================
-- TABELA: itens_pedido
-- ============================================================================
CREATE TABLE itens_pedido (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id UUID NOT NULL,
    produto_id UUID NOT NULL,
    produto_nome VARCHAR(200) NOT NULL,
    quantidade INTEGER NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,

    -- Constraints
    CONSTRAINT fk_item_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedidos(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_produto FOREIGN KEY (produto_id)
        REFERENCES produtos(id) ON DELETE RESTRICT,
    CONSTRAINT chk_item_quantidade CHECK (quantidade > 0),
    CONSTRAINT chk_item_preco CHECK (preco_unitario >= 0),
    CONSTRAINT chk_item_subtotal CHECK (subtotal >= 0)
);

-- Índices para performance
CREATE INDEX idx_item_pedido_pedido ON itens_pedido(pedido_id);
CREATE INDEX idx_item_pedido_produto ON itens_pedido(produto_id);

-- Comentários
COMMENT ON TABLE itens_pedido IS 'Itens dos pedidos (snapshot dos dados do produto)';
COMMENT ON COLUMN itens_pedido.produto_nome IS 'Nome do produto no momento da compra (snapshot)';
COMMENT ON COLUMN itens_pedido.preco_unitario IS 'Preço unitário no momento da compra (snapshot)';
COMMENT ON COLUMN itens_pedido.subtotal IS 'Quantidade × Preço Unitário';

-- ============================================================================
-- FIM DA MIGRATION
-- ============================================================================