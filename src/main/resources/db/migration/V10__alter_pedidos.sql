-- ========================================================================
-- Migration: Adicionar campos de rastreamento na tabela pedidos
-- Versão: V10__add_rastreamento_fields_to_pedidos.sql
-- Data: 2026-02-05
-- Descrição: Adiciona os campos codigo_rastreio e transportadora para
--            suportar o rastreamento de pedidos enviados.
-- ========================================================================

-- Verificar se coluna codigo_rastreio NÃO existe antes de adicionar
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'pedidos' 
        AND column_name = 'codigo_rastreio'
    ) THEN
        ALTER TABLE pedidos 
        ADD COLUMN codigo_rastreio VARCHAR(100);
        
        RAISE NOTICE 'Coluna codigo_rastreio adicionada';
    ELSE
        RAISE NOTICE 'Coluna codigo_rastreio já existe';
    END IF;
END $$;

-- Verificar se coluna transportadora NÃO existe antes de adicionar
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'pedidos' 
        AND column_name = 'transportadora'
    ) THEN
        ALTER TABLE pedidos 
        ADD COLUMN transportadora VARCHAR(100);
        
        RAISE NOTICE 'Coluna transportadora adicionada';
    ELSE
        RAISE NOTICE 'Coluna transportadora já existe';
    END IF;
END $$;

-- Adicionar índice para facilitar consultas por código de rastreio
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE indexname = 'idx_pedidos_codigo_rastreio'
    ) THEN
        CREATE INDEX idx_pedidos_codigo_rastreio ON pedidos(codigo_rastreio);
        RAISE NOTICE 'Índice idx_pedidos_codigo_rastreio criado';
    ELSE
        RAISE NOTICE 'Índice idx_pedidos_codigo_rastreio já existe';
    END IF;
END $$;

-- Adicionar comentários nas colunas (PostgreSQL)
COMMENT ON COLUMN pedidos.codigo_rastreio IS 'Código de rastreamento fornecido pela transportadora';
COMMENT ON COLUMN pedidos.transportadora IS 'Nome da transportadora responsável pela entrega';

-- ========================================================================
-- Verificação final
-- ========================================================================

-- Verificar se colunas foram adicionadas
DO $$
DECLARE
    col_codigo_rastreio BOOLEAN;
    col_transportadora BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'pedidos' AND column_name = 'codigo_rastreio'
    ) INTO col_codigo_rastreio;
    
    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'pedidos' AND column_name = 'transportadora'
    ) INTO col_transportadora;
    
    IF col_codigo_rastreio AND col_transportadora THEN
        RAISE NOTICE 'Migration V10 concluída com sucesso!';
        RAISE NOTICE 'Colunas codigo_rastreio e transportadora disponíveis';
    ELSE
        RAISE EXCEPTION 'Erro: Colunas não foram criadas corretamente';
    END IF;
END $$;

-- ========================================================================
-- Notas:
-- - Migration é idempotente (pode ser executada múltiplas vezes)
-- - Usa blocos DO $$ para verificar existência antes de criar
-- - Adiciona índice para otimizar buscas por código de rastreio
-- - Colunas são nullable (opcional) pois só existem após envio
-- ========================================================================