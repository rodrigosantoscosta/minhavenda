-- ========================================================================
-- Migration: Adicionar campos de rastreamento na tabela pedidos
-- Versão: V5__add_rastreamento_fields_to_pedidos.sql
-- Data: 2026-02-01
-- Descrição: Adiciona os campos codigo_rastreio e transportadora para
--            suportar o rastreamento de pedidos enviados.
-- ========================================================================

-- Adicionar coluna codigo_rastreio
ALTER TABLE pedidos
ADD COLUMN codigo_rastreio VARCHAR(100);

-- Adicionar comentário na coluna (PostgreSQL)
COMMENT ON COLUMN pedidos.codigo_rastreio IS 'Código de rastreamento fornecido pela transportadora';

-- Adicionar coluna transportadora
ALTER TABLE pedidos
ADD COLUMN transportadora VARCHAR(100);

-- Adicionar comentário na coluna (PostgreSQL)
COMMENT ON COLUMN pedidos.transportadora IS 'Nome da transportadora responsável pela entrega';

-- Adicionar índice para facilitar consultas por código de rastreio
CREATE INDEX idx_pedidos_codigo_rastreio ON pedidos(codigo_rastreio);

-- ========================================================================
-- Notas:
-- - Os campos são opcionais (nullable) pois só existem após o envio
-- - Índice criado para facilitar busca por código de rastreio
-- - Se precisar rollback, execute o script de reversão
-- ========================================================================
