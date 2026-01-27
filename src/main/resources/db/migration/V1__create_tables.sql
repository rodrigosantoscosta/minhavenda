-- ========================================
-- V1 - CRIAÇÃO DE TABELAS
-- Sistema de E-commerce
-- PostgreSQL
-- ========================================

-- Habilitar extensão para UUID (PostgreSQL < 13)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ========================================
-- TABELA: categorias
-- ========================================
CREATE TABLE categorias (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao VARCHAR(500),
    ativo BOOLEAN DEFAULT TRUE NOT NULL,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE categorias IS 'Categorias de produtos do e-commerce';

-- ========================================
-- TABELA: usuarios
-- ========================================
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('ADMIN', 'CLIENTE')),
    ativo BOOLEAN DEFAULT TRUE NOT NULL,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE usuarios IS 'Usuários do sistema (administradores e clientes)';
COMMENT ON COLUMN usuarios.senha IS 'Senha hasheada com BCrypt';

-- ========================================
-- TABELA: produtos
-- ========================================
CREATE TABLE produtos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(150) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10,2) NOT NULL CHECK (preco >= 0),
    moeda VARCHAR(3) DEFAULT 'BRL' NOT NULL,
    url_imagem VARCHAR(255),
    peso_kg DECIMAL(10,2) CHECK (peso_kg >= 0),
    altura_cm INTEGER CHECK (altura_cm >= 0 AND altura_cm <= 1000),
    largura_cm INTEGER CHECK (largura_cm >= 0 AND largura_cm <= 1000),
    comprimento_cm INTEGER CHECK (comprimento_cm >= 0 AND comprimento_cm <= 1000),
    categoria_id BIGINT,
    ativo BOOLEAN DEFAULT TRUE NOT NULL,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_produtos_categoria
        FOREIGN KEY (categoria_id)
        REFERENCES categorias(id)
        ON DELETE SET NULL
);

COMMENT ON TABLE produtos IS 'Produtos disponíveis para venda';
COMMENT ON COLUMN produtos.preco IS 'Valor monetário do produto';
COMMENT ON COLUMN produtos.moeda IS 'Código da moeda (BRL, USD, EUR)';

-- ========================================
-- TABELA: estoques
-- ========================================
CREATE TABLE estoques (
    id BIGSERIAL PRIMARY KEY,
    produto_id UUID NOT NULL UNIQUE,
    quantidade INTEGER DEFAULT 0 NOT NULL CHECK (quantidade >= 0),
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_estoques_produto
        FOREIGN KEY (produto_id)
        REFERENCES produtos(id)
        ON DELETE CASCADE
);

COMMENT ON TABLE estoques IS 'Controle de estoque dos produtos';
COMMENT ON COLUMN estoques.quantidade IS 'Quantidade disponível em estoque';

-- ========================================
-- TABELA: carrinhos
-- ========================================
CREATE TABLE carrinhos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO'
        CHECK (status IN ('ATIVO', 'FINALIZADO', 'ABANDONADO')),
    valor_total DECIMAL(10,2) DEFAULT 0.00 NOT NULL CHECK (valor_total >= 0),
    quantidade_total INTEGER DEFAULT 0 NOT NULL CHECK (quantidade_total >= 0),
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_carrinhos_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
);

COMMENT ON TABLE carrinhos IS 'Carrinhos de compras dos usuários';
COMMENT ON COLUMN carrinhos.quantidade_total IS 'Soma das quantidades de todos os itens';

-- ========================================
-- TABELA: itens_carrinho
-- ========================================
CREATE TABLE itens_carrinho (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    carrinho_id UUID NOT NULL,
    produto_id UUID NOT NULL,
    quantidade INTEGER NOT NULL CHECK (quantidade > 0),
    preco_unitario DECIMAL(10,2) NOT NULL CHECK (preco_unitario >= 0),
    subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),

    CONSTRAINT fk_itens_carrinho_carrinho
        FOREIGN KEY (carrinho_id)
        REFERENCES carrinhos(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_itens_carrinho_produto
        FOREIGN KEY (produto_id)
        REFERENCES produtos(id)
        ON DELETE RESTRICT,

    CONSTRAINT uk_carrinho_produto
        UNIQUE (carrinho_id, produto_id)
);

COMMENT ON TABLE itens_carrinho IS 'Itens dentro dos carrinhos de compras';

-- ========================================
-- TABELA: pedidos
-- ========================================
CREATE TABLE pedidos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CRIADO'
        CHECK (status IN ('CRIADO', 'PAGO', 'ENVIADO', 'ENTREGUE', 'CANCELADO')),
    subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),
    valor_frete DECIMAL(10,2) DEFAULT 0.00 NOT NULL CHECK (valor_frete >= 0),
    valor_desconto DECIMAL(10,2) DEFAULT 0.00 NOT NULL CHECK (valor_desconto >= 0),
    valor_total DECIMAL(10,2) NOT NULL CHECK (valor_total >= 0),
    endereco_entrega VARCHAR(500) NOT NULL,
    observacoes VARCHAR(1000),
    quantidade_itens INTEGER NOT NULL CHECK (quantidade_itens > 0),
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    data_pagamento TIMESTAMP,
    data_envio TIMESTAMP,
    data_entrega TIMESTAMP,

    CONSTRAINT fk_pedidos_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE RESTRICT
);

COMMENT ON TABLE pedidos IS 'Pedidos realizados pelos clientes';
COMMENT ON COLUMN pedidos.quantidade_itens IS 'Total de itens no pedido (soma das quantidades)';

-- ========================================
-- TABELA: itens_pedido
-- ========================================
CREATE TABLE itens_pedido (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id UUID NOT NULL,
    produto_id UUID NOT NULL,
    produto_nome VARCHAR(200) NOT NULL,
    quantidade INTEGER NOT NULL CHECK (quantidade > 0),
    preco_unitario DECIMAL(10,2) NOT NULL CHECK (preco_unitario >= 0),
    subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),

    CONSTRAINT fk_itens_pedido_pedido
        FOREIGN KEY (pedido_id)
        REFERENCES pedidos(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_itens_pedido_produto
        FOREIGN KEY (produto_id)
        REFERENCES produtos(id)
        ON DELETE RESTRICT
);

COMMENT ON TABLE itens_pedido IS 'Itens que compõem cada pedido';
COMMENT ON COLUMN itens_pedido.produto_nome IS 'Snapshot do nome do produto no momento da compra';

-- ========================================
-- TABELA: pagamentos
-- ========================================
CREATE TABLE pagamentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id UUID NOT NULL,
    metodo VARCHAR(30) NOT NULL CHECK (metodo IN ('CARTAO', 'PIX', 'BOLETO')),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'APPROVED', 'FAILED')),
    valor DECIMAL(10,2) NOT NULL CHECK (valor >= 0),
    moeda_pagamento VARCHAR(3) DEFAULT 'BRL' NOT NULL,
    processado_em TIMESTAMP,

    CONSTRAINT fk_pagamentos_pedido
        FOREIGN KEY (pedido_id)
        REFERENCES pedidos(id)
        ON DELETE CASCADE
);

COMMENT ON TABLE pagamentos IS 'Registros de pagamentos dos pedidos';
COMMENT ON COLUMN pagamentos.status IS 'PENDING: Pendente, APPROVED: Aprovado, FAILED: Falhou';

-- ========================================
-- TABELA: entregas
-- ========================================
CREATE TABLE entregas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id UUID NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED'
        CHECK (status IN ('CREATED', 'SHIPPED', 'DELIVERED')),
    endereco_entrega TEXT NOT NULL,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_entregas_pedido
        FOREIGN KEY (pedido_id)
        REFERENCES pedidos(id)
        ON DELETE CASCADE
);

COMMENT ON TABLE entregas IS 'Informações de entrega dos pedidos';
COMMENT ON COLUMN entregas.status IS 'CREATED: Criada, SHIPPED: Enviada, DELIVERED: Entregue';

-- ========================================
-- TABELA: notificacoes
-- ========================================
CREATE TABLE notificacoes (
    id BIGSERIAL PRIMARY KEY,
    usuario_id UUID NOT NULL,
    tipo VARCHAR(30) NOT NULL CHECK (tipo IN ('EMAIL', 'SMS')),
    mensagem TEXT NOT NULL,
    enviado BOOLEAN DEFAULT FALSE NOT NULL,
    enviado_em TIMESTAMP,

    CONSTRAINT fk_notificacoes_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
);

COMMENT ON TABLE notificacoes IS 'Notificações enviadas aos usuários';
COMMENT ON COLUMN notificacoes.enviado IS 'Indica se a notificação foi enviada com sucesso';

-- ========================================
-- TABELA: eventos_dominio
-- ========================================
CREATE TABLE eventos_dominio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo VARCHAR(100) NOT NULL,
    payload JSON NOT NULL,
    data_publicacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE eventos_dominio IS 'Eventos de domínio para auditoria e integração';
COMMENT ON COLUMN eventos_dominio.payload IS 'Dados do evento em formato JSON';

-- ========================================
-- FIM DA MIGRATION V1
-- ========================================