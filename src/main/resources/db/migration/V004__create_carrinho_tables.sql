-- V004__create_carrinho_tables.sql
-- Migration para criar tabelas de carrinho de compras

-- Tabela de Carrinho
CREATE TABLE IF NOT EXISTS carrinhos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    valor_total NUMERIC(10, 2) NOT NULL DEFAULT 0,
    quantidade_total INTEGER NOT NULL DEFAULT 0,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT carrinhos_status_check CHECK (status IN ('ATIVO', 'FINALIZADO', 'ABANDONADO')),
    CONSTRAINT carrinhos_valor_check CHECK (valor_total >= 0),
    CONSTRAINT carrinhos_quantidade_check CHECK (quantidade_total >= 0),
    CONSTRAINT fk_carrinhos_usuario FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Tabela de Itens do Carrinho
CREATE TABLE IF NOT EXISTS itens_carrinho (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    carrinho_id UUID NOT NULL,
    produto_id UUID NOT NULL,
    quantidade INTEGER NOT NULL,
    preco_unitario NUMERIC(10, 2) NOT NULL,
    subtotal NUMERIC(10, 2) NOT NULL,
    data_adicao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT itens_carrinho_quantidade_check CHECK (quantidade > 0),
    CONSTRAINT itens_carrinho_preco_check CHECK (preco_unitario >= 0),
    CONSTRAINT itens_carrinho_subtotal_check CHECK (subtotal >= 0),
    CONSTRAINT fk_itens_carrinho_carrinho FOREIGN KEY (carrinho_id) 
        REFERENCES carrinhos(id) ON DELETE CASCADE,
    CONSTRAINT fk_itens_carrinho_produto FOREIGN KEY (produto_id) 
        REFERENCES produtos(id) ON DELETE CASCADE,
    CONSTRAINT uk_carrinho_produto UNIQUE (carrinho_id, produto_id)
);

-- Índices para Carrinhos
CREATE INDEX idx_carrinhos_usuario ON carrinhos(usuario_id);
CREATE INDEX idx_carrinhos_status ON carrinhos(status);
CREATE INDEX idx_carrinhos_data_atualizacao ON carrinhos(data_atualizacao DESC);
CREATE UNIQUE INDEX idx_carrinhos_usuario_ativo ON carrinhos(usuario_id, status) 
    WHERE status = 'ATIVO';

-- Índices para Itens do Carrinho
CREATE INDEX idx_itens_carrinho_carrinho ON itens_carrinho(carrinho_id);
CREATE INDEX idx_itens_carrinho_produto ON itens_carrinho(produto_id);
CREATE INDEX idx_itens_carrinho_data ON itens_carrinho(data_adicao DESC);

-- Comentários - Carrinhos
COMMENT ON TABLE carrinhos IS 'Tabela de carrinhos de compras dos usuários';
COMMENT ON COLUMN carrinhos.id IS 'Identificador único do carrinho (UUID)';
COMMENT ON COLUMN carrinhos.usuario_id IS 'Referência ao usuário dono do carrinho';
COMMENT ON COLUMN carrinhos.status IS 'Status do carrinho: ATIVO, FINALIZADO ou ABANDONADO';
COMMENT ON COLUMN carrinhos.valor_total IS 'Valor total de todos os itens';
COMMENT ON COLUMN carrinhos.quantidade_total IS 'Quantidade total de itens';

-- Comentários - Itens do Carrinho
COMMENT ON TABLE itens_carrinho IS 'Tabela de itens dentro dos carrinhos';
COMMENT ON COLUMN itens_carrinho.id IS 'Identificador único do item (UUID)';
COMMENT ON COLUMN itens_carrinho.carrinho_id IS 'Referência ao carrinho';
COMMENT ON COLUMN itens_carrinho.produto_id IS 'Referência ao produto';
COMMENT ON COLUMN itens_carrinho.preco_unitario IS 'Preço do produto no momento da adição';
COMMENT ON COLUMN itens_carrinho.subtotal IS 'Preço unitário × quantidade';

-- Trigger para atualizar data_atualizacao do carrinho
CREATE OR REPLACE FUNCTION update_carrinho_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_atualizacao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_carrinho_timestamp
    BEFORE UPDATE ON carrinhos
    FOR EACH ROW
    EXECUTE FUNCTION update_carrinho_timestamp();

-- Trigger para recalcular totais do carrinho quando item é adicionado/atualizado/removido
CREATE OR REPLACE FUNCTION recalcular_totais_carrinho()
RETURNS TRIGGER AS $$
DECLARE
    v_carrinho_id UUID;
BEGIN
    -- Determinar o carrinho_id baseado na operação
    IF TG_OP = 'DELETE' THEN
        v_carrinho_id := OLD.carrinho_id;
    ELSE
        v_carrinho_id := NEW.carrinho_id;
    END IF;
    
    -- Atualizar totais do carrinho
    UPDATE carrinhos
    SET 
        valor_total = COALESCE((
            SELECT SUM(subtotal)
            FROM itens_carrinho
            WHERE carrinho_id = v_carrinho_id
        ), 0),
        quantidade_total = COALESCE((
            SELECT SUM(quantidade)
            FROM itens_carrinho
            WHERE carrinho_id = v_carrinho_id
        ), 0),
        data_atualizacao = CURRENT_TIMESTAMP
    WHERE id = v_carrinho_id;
    
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_recalcular_totais_insert
    AFTER INSERT ON itens_carrinho
    FOR EACH ROW
    EXECUTE FUNCTION recalcular_totais_carrinho();

CREATE TRIGGER trigger_recalcular_totais_update
    AFTER UPDATE ON itens_carrinho
    FOR EACH ROW
    EXECUTE FUNCTION recalcular_totais_carrinho();

CREATE TRIGGER trigger_recalcular_totais_delete
    AFTER DELETE ON itens_carrinho
    FOR EACH ROW
    EXECUTE FUNCTION recalcular_totais_carrinho();
