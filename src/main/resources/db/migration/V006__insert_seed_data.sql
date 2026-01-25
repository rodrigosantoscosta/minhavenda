-- V006__insert_seed_data.sql
-- Migration para inserir dados iniciais (seed)

-- ==========================================
-- 1. USUÁRIOS
-- ==========================================

-- Senha dos usuários: 'senha123' (BCrypt hash)
-- Hash gerado com: BCryptPasswordEncoder().encode("senha123")
-- Senha admin: 'admin123'

INSERT INTO usuarios (id, nome, email, senha, tipo, ativo) VALUES
    ('550e8400-e29b-41d4-a716-446655440000', 'Administrador', 'admin@minhavenda.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IKdZCLfQGHZ7zPLFqPGQKQP8k.H8m6', 'ADMIN', true),
    ('550e8400-e29b-41d4-a716-446655440001', 'João Silva', 'joao@teste.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IKdZCLfQGHZ7zPLFqPGQKQP8k.H8m6', 'CLIENTE', true),
    ('550e8400-e29b-41d4-a716-446655440002', 'Maria Santos', 'maria@teste.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IKdZCLfQGHZ7zPLFqPGQKQP8k.H8m6', 'CLIENTE', true),
    ('550e8400-e29b-41d4-a716-446655440003', 'Pedro Oliveira', 'pedro@teste.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IKdZCLfQGHZ7zPLFqPGQKQP8k.H8m6', 'CLIENTE', true);

-- ==========================================
-- 2. CATEGORIAS
-- ==========================================

INSERT INTO categorias (id, nome, descricao, ativa) VALUES
    (1, 'Eletrônicos', 'Produtos eletrônicos e tecnologia', true),
    (2, 'Informática', 'Computadores, periféricos e acessórios', true),
    (3, 'Smartphones', 'Celulares e acessórios', true),
    (4, 'Games', 'Consoles, jogos e acessórios para games', true),
    (5, 'Áudio', 'Fones, caixas de som e equipamentos de áudio', true),
    (6, 'Smart Home', 'Produtos de automação residencial', true),
    (7, 'Livros', 'Livros físicos e digitais', true),
    (8, 'Esportes', 'Artigos esportivos e fitness', true);

-- Resetar sequence para próximo ID
SELECT setval('categorias_id_seq', (SELECT MAX(id) FROM categorias));

-- ==========================================
-- 3. PRODUTOS
-- ==========================================

INSERT INTO produtos (id, nome, descricao, preco_valor, preco_moeda, ativo, categoria_id) VALUES
    -- Eletrônicos
    ('650e8400-e29b-41d4-a716-446655440001', 'Smart TV 50" 4K', 'Smart TV LED 50 polegadas com resolução 4K UHD', 2499.90, 'BRL', true, 1),
    ('650e8400-e29b-41d4-a716-446655440002', 'Soundbar JBL', 'Soundbar com subwoofer wireless e Bluetooth', 899.90, 'BRL', true, 5),
    
    -- Informática
    ('650e8400-e29b-41d4-a716-446655440003', 'Notebook Dell Inspiron', 'Notebook i5, 8GB RAM, SSD 256GB', 3299.00, 'BRL', true, 2),
    ('650e8400-e29b-41d4-a716-446655440004', 'Mouse Logitech MX Master', 'Mouse sem fio ergonômico para produtividade', 449.90, 'BRL', true, 2),
    ('650e8400-e29b-41d4-a716-446655440005', 'Teclado Mecânico RGB', 'Teclado mecânico gamer com iluminação RGB', 599.00, 'BRL', true, 2),
    ('650e8400-e29b-41d4-a716-446655440006', 'Monitor 27" Full HD', 'Monitor LED 27 polegadas 75Hz', 899.00, 'BRL', true, 2),
    
    -- Smartphones
    ('650e8400-e29b-41d4-a716-446655440007', 'iPhone 14 Pro 256GB', 'iPhone 14 Pro com Dynamic Island', 7999.00, 'BRL', true, 3),
    ('650e8400-e29b-41d4-a716-446655440008', 'Samsung Galaxy S23', 'Galaxy S23 5G 128GB', 3499.00, 'BRL', true, 3),
    ('650e8400-e29b-41d4-a716-446655440009', 'Xiaomi Redmi Note 12', 'Redmi Note 12 128GB', 1299.00, 'BRL', true, 3),
    
    -- Games
    ('650e8400-e29b-41d4-a716-446655440010', 'PlayStation 5', 'Console PlayStation 5 com leitor de disco', 4499.00, 'BRL', true, 4),
    ('650e8400-e29b-41d4-a716-446655440011', 'Xbox Series S', 'Console Xbox Series S 512GB', 2399.00, 'BRL', true, 4),
    ('650e8400-e29b-41d4-a716-446655440012', 'Controle DualSense', 'Controle sem fio PS5 DualSense', 449.90, 'BRL', true, 4),
    
    -- Áudio
    ('650e8400-e29b-41d4-a716-446655440013', 'AirPods Pro 2ª Geração', 'Fones de ouvido com cancelamento de ruído', 2199.00, 'BRL', true, 5),
    ('650e8400-e29b-41d4-a716-446655440014', 'JBL Flip 6', 'Caixa de som Bluetooth portátil', 699.00, 'BRL', true, 5),
    ('650e8400-e29b-41d4-a716-446655440015', 'Sony WH-1000XM5', 'Fone de ouvido over-ear com cancelamento de ruído', 2799.00, 'BRL', true, 5),
    
    -- Smart Home
    ('650e8400-e29b-41d4-a716-446655440016', 'Echo Dot 5ª Geração', 'Smart speaker com Alexa', 349.00, 'BRL', true, 6),
    ('650e8400-e29b-41d4-a716-446655440017', 'Lâmpada Inteligente', 'Lâmpada LED RGB WiFi', 79.90, 'BRL', true, 6),
    ('650e8400-e29b-41d4-a716-446655440018', 'Câmera de Segurança', 'Câmera WiFi Full HD com visão noturna', 299.00, 'BRL', true, 6),
    
    -- Livros
    ('650e8400-e29b-41d4-a716-446655440019', 'Clean Code', 'Livro sobre boas práticas de programação', 89.90, 'BRL', true, 7),
    ('650e8400-e29b-41d4-a716-446655440020', 'Domain-Driven Design', 'Livro sobre DDD por Eric Evans', 129.90, 'BRL', true, 7);

-- ==========================================
-- 4. ESTOQUE
-- ==========================================

INSERT INTO estoque (produto_id, quantidade) VALUES
    -- Eletrônicos
    ('650e8400-e29b-41d4-a716-446655440001', 15),
    ('650e8400-e29b-41d4-a716-446655440002', 25),
    
    -- Informática
    ('650e8400-e29b-41d4-a716-446655440003', 10),
    ('650e8400-e29b-41d4-a716-446655440004', 50),
    ('650e8400-e29b-41d4-a716-446655440005', 30),
    ('650e8400-e29b-41d4-a716-446655440006', 20),
    
    -- Smartphones
    ('650e8400-e29b-41d4-a716-446655440007', 8),
    ('650e8400-e29b-41d4-a716-446655440008', 12),
    ('650e8400-e29b-41d4-a716-446655440009', 40),
    
    -- Games
    ('650e8400-e29b-41d4-a716-446655440010', 5),
    ('650e8400-e29b-41d4-a716-446655440011', 15),
    ('650e8400-e29b-41d4-a716-446655440012', 35),
    
    -- Áudio
    ('650e8400-e29b-41d4-a716-446655440013', 20),
    ('650e8400-e29b-41d4-a716-446655440014', 45),
    ('650e8400-e29b-41d4-a716-446655440015', 10),
    
    -- Smart Home
    ('650e8400-e29b-41d4-a716-446655440016', 60),
    ('650e8400-e29b-41d4-a716-446655440017', 100),
    ('650e8400-e29b-41d4-a716-446655440018', 30),
    
    -- Livros
    ('650e8400-e29b-41d4-a716-446655440019', 50),
    ('650e8400-e29b-41d4-a716-446655440020', 35);

-- ==========================================
-- COMENTÁRIOS
-- ==========================================

COMMENT ON TABLE usuarios IS 'Dados de seed incluem 1 admin e 3 clientes';
COMMENT ON TABLE categorias IS 'Dados de seed incluem 8 categorias principais';
COMMENT ON TABLE produtos IS 'Dados de seed incluem 20 produtos de exemplo';
COMMENT ON TABLE estoque IS 'Estoque inicial configurado para todos os produtos';
