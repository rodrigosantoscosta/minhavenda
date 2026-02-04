-- ========================================
-- V4 - INSERT USUARIOS
-- Dados iniciais de usuários
-- ========================================

-- ========================================

-- Usuário Administrador
INSERT INTO usuarios (nome, email, senha, tipo, ativo) VALUES
('Administrador', 'admin@loja.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', true);

-- Usuários Clientes
INSERT INTO usuarios (nome, email, senha, tipo, ativo) VALUES
('João Silva', 'joao.silva@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENTE', true),
('Maria Santos', 'maria.santos@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENTE', true),
('Pedro Oliveira', 'pedro.oliveira@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENTE', true),
('Ana Costa', 'ana.costa@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENTE', true),
('Carlos Ferreira', 'carlos.ferreira@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENTE', true);

-- ========================================
-- RESULTADO: 6 usuários inseridos
-- 1 ADMIN + 5 CLIENTES
-- IDs gerados automaticamente (UUID)
-- ========================================

-- ========================================
-- CREDENCIAIS DE LOGIN:
-- ========================================
-- admin@loja.com / senha123 (ADMIN)
-- joao.silva@email.com / senha123
-- maria.santos@email.com / senha123
-- pedro.oliveira@email.com / senha123
-- ana.costa@email.com / senha123
-- carlos.ferreira@email.com / senha123
-- ========================================
