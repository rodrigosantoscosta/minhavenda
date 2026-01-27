-- ========================================
-- V2 - CRIAÇÃO DE ÍNDICES - VERSÃO CORRIGIDA
-- Otimização de Performance
-- PostgreSQL
-- ========================================

-- ========================================
-- ÍNDICES: categorias
-- ========================================
CREATE INDEX idx_categoria_ativo ON categorias(ativo) WHERE ativo = TRUE;
CREATE INDEX idx_categoria_nome ON categorias(nome);

-- ========================================
-- ÍNDICES: usuarios
-- ========================================
CREATE INDEX idx_usuario_email ON usuarios(email);
CREATE INDEX idx_usuario_tipo ON usuarios(tipo);
CREATE INDEX idx_usuario_ativo ON usuarios(ativo) WHERE ativo = TRUE;

-- ========================================
-- ÍNDICES: produtos
-- ========================================
CREATE INDEX idx_produto_categoria ON produtos(categoria_id);
CREATE INDEX idx_produto_ativo ON produtos(ativo) WHERE ativo = TRUE;
CREATE INDEX idx_produto_nome ON produtos(nome);
CREATE INDEX idx_produto_preco ON produtos(preco);

-- ========================================
-- ÍNDICES: estoques
-- ========================================
CREATE UNIQUE INDEX idx_estoque_produto ON estoques(produto_id);

-- ========================================
-- ÍNDICES: carrinhos
-- ========================================
CREATE INDEX idx_carrinho_usuario ON carrinhos(usuario_id);
CREATE INDEX idx_carrinho_status ON carrinhos(status);
CREATE INDEX idx_carrinho_data_criacao ON carrinhos(data_criacao DESC);

-- ========================================
-- ÍNDICES: itens_carrinho
-- ========================================
CREATE INDEX idx_item_carrinho_carrinho ON itens_carrinho(carrinho_id);
CREATE INDEX idx_item_carrinho_produto ON itens_carrinho(produto_id);

-- ========================================
-- ÍNDICES: pedidos
-- ========================================
CREATE INDEX idx_pedido_usuario ON pedidos(usuario_id);
CREATE INDEX idx_pedido_status ON pedidos(status);
CREATE INDEX idx_pedido_data ON pedidos(data_criacao DESC);
CREATE INDEX idx_pedido_data_pagamento ON pedidos(data_pagamento DESC) WHERE data_pagamento IS NOT NULL;

-- ========================================
-- ÍNDICES: itens_pedido
-- ========================================
CREATE INDEX idx_item_pedido ON itens_pedido(pedido_id);
CREATE INDEX idx_item_produto ON itens_pedido(produto_id);

-- ========================================
-- ÍNDICES: pagamentos
-- ========================================
CREATE INDEX idx_pagamento_pedido ON pagamentos(pedido_id);
CREATE INDEX idx_pagamento_status ON pagamentos(status);

-- ========================================
-- ÍNDICES: entregas
-- ========================================
CREATE INDEX idx_entrega_pedido ON entregas(pedido_id);
CREATE INDEX idx_entrega_status ON entregas(status);

-- ========================================
-- ÍNDICES: notificacoes
-- ========================================
CREATE INDEX idx_notificacao_usuario ON notificacoes(usuario_id);
CREATE INDEX idx_notificacao_enviado ON notificacoes(enviado);

-- ========================================
-- ÍNDICES: eventos_dominio
-- ========================================
CREATE INDEX idx_evento_tipo ON eventos_dominio(tipo);
CREATE INDEX idx_evento_data ON eventos_dominio(data_publicacao DESC);

-- ========================================
-- FIM DA MIGRATION V2
-- ========================================