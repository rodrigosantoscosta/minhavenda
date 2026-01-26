-- V001__create_usuarios_table.sql
-- Migration para criar tabela de usuários

CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senhaHash VARCHAR(255) NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('ADMIN', 'CLIENTE')),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT usuarios_nome_check CHECK (LENGTH(nome) >= 3),
    CONSTRAINT usuarios_email_check CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Índices para melhor performance
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_tipo ON usuarios(tipo);
CREATE INDEX idx_usuarios_ativo ON usuarios(ativo);

-- Comentários
COMMENT ON TABLE usuarios IS 'Tabela de usuários do sistema (ADMIN e CLIENTE)';
COMMENT ON COLUMN usuarios.id IS 'Identificador único do usuário (UUID)';
COMMENT ON COLUMN usuarios.email IS 'Email do usuário (unique)';
COMMENT ON COLUMN usuarios.senha IS 'Senha criptografada com BCrypt';
COMMENT ON COLUMN usuarios.tipo IS 'Tipo de usuário: ADMIN ou CLIENTE';
