-- V003__create_produtos_estoque_tables.sql
-- Migration para criar tabelas de produtos e estoque

-- Tabela de Produtos
CREATE TABLE IF NOT EXISTS produtos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500),
    preco_valor NUMERIC(10, 2) NOT NULL,
    preco_moeda VARCHAR(3) NOT NULL DEFAULT 'BRL',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    categoria_id BIGINT,
    
    CONSTRAINT produtos_nome_check CHECK (LENGTH(nome) >= 3),
    CONSTRAINT produtos_preco_check CHECK (preco_valor >= 0),
    CONSTRAINT fk_produtos_categoria FOREIGN KEY (categoria_id) 
        REFERENCES categorias(id) ON DELETE SET NULL
);

-- Tabela de Estoque
CREATE TABLE IF NOT EXISTS estoque (
    id BIGSERIAL PRIMARY KEY,
    produto_id UUID NOT NULL UNIQUE,
    quantidade INTEGER NOT NULL DEFAULT 0,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT estoque_quantidade_check CHECK (quantidade >= 0),
    CONSTRAINT fk_estoque_produto FOREIGN KEY (produto_id) 
        REFERENCES produtos(id) ON DELETE CASCADE
);

-- Índices para Produtos
CREATE INDEX idx_produtos_nome ON produtos(nome);
CREATE INDEX idx_produtos_ativo ON produtos(ativo);
CREATE INDEX idx_produtos_categoria ON produtos(categoria_id);
CREATE INDEX idx_produtos_preco ON produtos(preco_valor);
CREATE INDEX idx_produtos_data_cadastro ON produtos(data_cadastro DESC);

-- Índices para Estoque
CREATE UNIQUE INDEX idx_estoque_produto ON estoque(produto_id);
CREATE INDEX idx_estoque_quantidade ON estoque(quantidade);

-- Comentários - Produtos
COMMENT ON TABLE produtos IS 'Tabela de produtos do e-commerce';
COMMENT ON COLUMN produtos.id IS 'Identificador único do produto (UUID)';
COMMENT ON COLUMN produtos.preco_valor IS 'Valor monetário do produto';
COMMENT ON COLUMN produtos.preco_moeda IS 'Moeda do preço (padrão BRL)';
COMMENT ON COLUMN produtos.categoria_id IS 'Referência à categoria do produto';

-- Comentários - Estoque
COMMENT ON TABLE estoque IS 'Tabela de controle de estoque dos produtos';
COMMENT ON COLUMN estoque.id IS 'Identificador único do estoque (BIGSERIAL)';
COMMENT ON COLUMN estoque.produto_id IS 'Referência ao produto (OneToOne)';
COMMENT ON COLUMN estoque.quantidade IS 'Quantidade disponível em estoque';
COMMENT ON COLUMN estoque.atualizado_em IS 'Data/hora da última atualização';

-- Trigger para atualizar timestamp do estoque
CREATE OR REPLACE FUNCTION update_estoque_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.atualizado_em = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_estoque_timestamp
    BEFORE UPDATE ON estoque
    FOR EACH ROW
    EXECUTE FUNCTION update_estoque_timestamp();
