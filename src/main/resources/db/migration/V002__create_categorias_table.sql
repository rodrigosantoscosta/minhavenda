-- V002__create_categorias_table.sql
-- Migration para criar tabela de categorias

CREATE TABLE IF NOT EXISTS categorias (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500),
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT categorias_nome_check CHECK (LENGTH(nome) >= 3),
    CONSTRAINT categorias_nome_unique UNIQUE (nome)
);

-- Índices
CREATE INDEX idx_categorias_ativa ON categorias(ativa);
CREATE INDEX idx_categorias_nome ON categorias(nome);

-- Comentários
COMMENT ON TABLE categorias IS 'Tabela de categorias de produtos';
COMMENT ON COLUMN categorias.id IS 'Identificador único da categoria (BIGSERIAL)';
COMMENT ON COLUMN categorias.nome IS 'Nome da categoria (unique)';
COMMENT ON COLUMN categorias.ativa IS 'Indica se a categoria está ativa';
