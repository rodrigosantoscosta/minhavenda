-- ============================================================================
-- Migration V010: Insert de Dados de Teste
-- ============================================================================
-- Descrição: Insere dados iniciais para teste do sistema MinhaVenda
-- Data: 2026-01-21

-- ============================================================================

-- ============================================================================
-- TABELA: usuarios
-- ============================================================================

INSERT INTO usuarios (id, nome, email, senha_hash, tipo, ativo, data_cadastro) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Administrador', 'admin@minhavenda.com', '$2a$10$N8qQ4j5YvF.sGxkBfJZxL.VjZQx8qhE7XNZZUqZ2Z2Z2Z2Z2Z2Z2Z2', 'ADMIN', true, NOW()),
    ('22222222-2222-2222-2222-222222222222', 'João Silva', 'joao@email.com', '$2a$10$N8qQ4j5YvF.sGxkBfJZxL.VjZQx8qhE7XNZZUqZ2Z2Z2Z2Z2Z2Z2Z2', 'CLIENTE', true, NOW()),
    ('33333333-3333-3333-3333-333333333333', 'Maria Santos', 'maria@email.com', '$2a$10$N8qQ4j5YvF.sGxkBfJZxL.VjZQx8qhE7XNZZUqZ2Z2Z2Z2Z2Z2Z2Z2', 'CLIENTE', true, NOW()),
    ('44444444-4444-4444-4444-444444444444', 'Pedro Oliveira', 'pedro@email.com', '$2a$10$N8qQ4j5YvF.sGxkBfJZxL.VjZQx8qhE7XNZZUqZ2Z2Z2Z2Z2Z2Z2Z2', 'CLIENTE', true, NOW()),
    ('55555555-5555-5555-5555-555555555555', 'Ana Costa', 'ana@email.com', '$2a$10$N8qQ4j5YvF.sGxkBfJZxL.VjZQx8qhE7XNZZUqZ2Z2Z2Z2Z2Z2Z2Z2', 'CLIENTE', true, NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- TABELA: categorias
-- ============================================================================
INSERT INTO categorias (id, nome, descricao, ativo, data_cadastro) VALUES
    (1, 'Eletrônicos', 'Produtos eletrônicos e tecnologia', true, NOW()),
    (2, 'Roupas', 'Vestuário e acessórios de moda', true, NOW()),
    (3, 'Livros', 'Livros físicos e digitais', true, NOW()),
    (4, 'Casa e Decoração', 'Móveis e itens decorativos', true, NOW()),
    (5, 'Esportes', 'Artigos esportivos e fitness', true, NOW()),
    (6, 'Alimentos', 'Alimentos e bebidas', true, NOW()),
    (7, 'Beleza', 'Cosméticos e produtos de beleza', true, NOW()),
    (8, 'Brinquedos', 'Brinquedos e jogos', true, NOW()),
    (9, 'Games', 'Jogos eletrônicos e consoles', true, NOW()),
    (10, 'Ferramentas', 'Ferramentas e equipamentos', true, NOW())
ON CONFLICT (id) DO NOTHING;

-- Resetar sequência de categorias
SELECT setval('categorias_id_seq', (SELECT MAX(id) FROM categorias));

-- ============================================================================
-- TABELA: produtos
-- ============================================================================
INSERT INTO produtos (id, nome, descricao, preco_valor, preco_moeda, ativo, data_cadastro, categoria_id) VALUES
    -- Eletrônicos
    ('a1111111-1111-1111-1111-111111111111', 'Smartphone XPhone 14', 'Smartphone premium com câmera de 48MP e 128GB', 2999.99, 'BRL', true, NOW(), 1),
    ('a2222222-2222-2222-2222-222222222222', 'Notebook UltraBook Pro', 'Notebook com Intel i7, 16GB RAM, SSD 512GB', 4599.00, 'BRL', true, NOW(), 1),
    ('a3333333-3333-3333-3333-333333333333', 'Fone Bluetooth Premium', 'Fone com cancelamento de ruído ativo', 599.90, 'BRL', true, NOW(), 1),
    ('a4444444-4444-4444-4444-444444444444', 'Smart TV 55" 4K', 'Smart TV LED 55 polegadas com 4K e HDR', 2199.00, 'BRL', true, NOW(), 1),
    ('a5555555-5555-5555-5555-555555555555', 'Tablet Pro 12"', 'Tablet com tela de 12 polegadas e caneta', 1899.00, 'BRL', true, NOW(), 1),
    
    -- Roupas
    ('b1111111-1111-1111-1111-111111111111', 'Camiseta Básica Premium', 'Camiseta 100% algodão egípcio', 89.90, 'BRL', true, NOW(), 2),
    ('b2222222-2222-2222-2222-222222222222', 'Calça Jeans Slim', 'Calça jeans com elastano, corte moderno', 149.90, 'BRL', true, NOW(), 2),
    ('b3333333-3333-3333-3333-333333333333', 'Jaqueta de Couro', 'Jaqueta de couro legítimo estilo motoqueiro', 599.00, 'BRL', true, NOW(), 2),
    ('b4444444-4444-4444-4444-444444444444', 'Tênis Running Pro', 'Tênis para corrida com tecnologia de amortecimento', 399.90, 'BRL', true, NOW(), 2),
    ('b5555555-5555-5555-5555-555555555555', 'Vestido Longo Elegante', 'Vestido longo para eventos especiais', 259.00, 'BRL', true, NOW(), 2),
    
    -- Livros
    ('c1111111-1111-1111-1111-111111111111', 'Clean Code', 'Livro sobre código limpo e boas práticas', 79.90, 'BRL', true, NOW(), 3),
    ('c2222222-2222-2222-2222-222222222222', 'Domain-Driven Design', 'DDD aplicado ao desenvolvimento de software', 89.90, 'BRL', true, NOW(), 3),
    ('c3333333-3333-3333-3333-333333333333', 'O Senhor dos Anéis - Coleção', 'Trilogia completa do clássico de fantasia', 149.90, 'BRL', true, NOW(), 3),
    ('c4444444-4444-4444-4444-444444444444', '1984', 'Clássico distópico de George Orwell', 44.90, 'BRL', true, NOW(), 3),
    ('c5555555-5555-5555-5555-555555555555', 'Harry Potter - Box Completo', 'Coleção completa das 7 obras', 299.90, 'BRL', true, NOW(), 3),
    
    -- Casa e Decoração
    ('d1111111-1111-1111-1111-111111111111', 'Sofá 3 Lugares Reclinável', 'Sofá confortável com assento retrátil', 1899.00, 'BRL', true, NOW(), 4),
    ('d2222222-2222-2222-2222-222222222222', 'Mesa de Jantar 6 Lugares', 'Mesa de madeira maciça com 6 cadeiras', 2499.00, 'BRL', true, NOW(), 4),
    ('d3333333-3333-3333-3333-333333333333', 'Luminária de Chão LED', 'Luminária moderna com iluminação ajustável', 349.90, 'BRL', true, NOW(), 4),
    ('d4444444-4444-4444-4444-444444444444', 'Quadro Abstrato 90x60cm', 'Arte moderna para decoração de parede', 189.00, 'BRL', true, NOW(), 4),
    ('d5555555-5555-5555-5555-555555555555', 'Jogo de Cama Casal Premium', 'Jogo de cama 200 fios egípcios', 279.90, 'BRL', true, NOW(), 4),
    
    -- Esportes
    ('e1111111-1111-1111-1111-111111111111', 'Bicicleta Mountain Bike 29', 'Bike aro 29 com suspensão dianteira', 1599.00, 'BRL', true, NOW(), 5),
    ('e2222222-2222-2222-2222-222222222222', 'Kit Halteres Ajustáveis 20kg', 'Par de halteres com anilhas removíveis', 299.90, 'BRL', true, NOW(), 5),
    ('e3333333-3333-3333-3333-333333333333', 'Esteira Elétrica Dobrável', 'Esteira compacta com monitor digital', 1899.00, 'BRL', true, NOW(), 5),
    ('e4444444-4444-4444-4444-444444444444', 'Bola de Futebol Oficial', 'Bola profissional tamanho oficial', 129.90, 'BRL', true, NOW(), 5),
    ('e5555555-5555-5555-5555-555555555555', 'Tapete de Yoga Premium', 'Tapete antiderrapante com 5mm de espessura', 89.90, 'BRL', true, NOW(), 5),
    
    -- Games
    ('f1111111-1111-1111-1111-111111111111', 'Console PlayStation 5', 'Console de última geração com SSD 825GB', 4299.00, 'BRL', true, NOW(), 9),
    ('f2222222-2222-2222-2222-222222222222', 'Controle Wireless DualSense', 'Controle sem fio com feedback háptico', 449.90, 'BRL', true, NOW(), 9),
    ('f3333333-3333-3333-3333-333333333333', 'Jogo FIFA 24', 'Game de futebol para PS5', 299.90, 'BRL', true, NOW(), 9),
    ('f4444444-4444-4444-4444-444444444444', 'Headset Gamer RGB', 'Fone gamer com microfone destacável', 259.00, 'BRL', true, NOW(), 9),
    ('f5555555-5555-5555-5555-555555555555', 'Cadeira Gamer Reclinável', 'Cadeira ergonômica com ajuste de altura', 899.00, 'BRL', true, NOW(), 9),
    
    -- Beleza
    ('g1111111-1111-1111-1111-111111111111', 'Kit Shampoo e Condicionador', 'Tratamento capilar profissional', 119.90, 'BRL', true, NOW(), 7),
    ('g2222222-2222-2222-2222-222222222222', 'Perfume Importado 100ml', 'Fragrância masculina amadeirada', 389.00, 'BRL', true, NOW(), 7),
    ('g3333333-3333-3333-3333-333333333333', 'Creme Facial Anti-idade', 'Hidratante com vitamina C e ácido hialurônico', 159.90, 'BRL', true, NOW(), 7),
    ('g4444444-4444-4444-4444-444444444444', 'Paleta de Maquiagem 40 Cores', 'Kit completo para maquiagem profissional', 199.00, 'BRL', true, NOW(), 7),
    ('g5555555-5555-5555-5555-555555555555', 'Escova Secadora Rotativa', 'Secador e modelador 2 em 1', 249.90, 'BRL', true, NOW(), 7)
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- TABELA: estoque
-- ============================================================================
-- Criar estoque para cada produto
INSERT INTO estoque (produto_id, quantidade, atualizado_em)
SELECT p.id, 
       CASE 
           WHEN p.preco_valor > 2000 THEN 5  -- Produtos caros: pouco estoque
           WHEN p.preco_valor > 500 THEN 15  -- Médio preço: estoque médio
           ELSE 50                            -- Baratos: muito estoque
       END as quantidade,
       NOW() as atualizado_em
FROM produtos p
WHERE NOT EXISTS (
    SELECT 1 FROM estoque e WHERE e.produto_id = p.id
);

-- ============================================================================
-- VERIFICAÇÕES
-- ============================================================================
-- Contar registros inseridos
DO $$
DECLARE
    total_usuarios INTEGER;
    total_categorias INTEGER;
    total_produtos INTEGER;
    total_estoque INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_usuarios FROM usuarios;
    SELECT COUNT(*) INTO total_categorias FROM categorias;
    SELECT COUNT(*) INTO total_produtos FROM produtos;
    SELECT COUNT(*) INTO total_estoque FROM estoque;
    
    RAISE NOTICE '============================================';
    RAISE NOTICE 'DADOS DE TESTE INSERIDOS COM SUCESSO!';
    RAISE NOTICE '============================================';
    RAISE NOTICE 'Total de usuários: %', total_usuarios;
    RAISE NOTICE 'Total de categorias: %', total_categorias;
    RAISE NOTICE 'Total de produtos: %', total_produtos;
    RAISE NOTICE 'Total de estoques: %', total_estoque;
    RAISE NOTICE '============================================';
    RAISE NOTICE 'Credenciais de teste:';
    RAISE NOTICE 'Admin: admin@minhavenda.com / admin123';
    RAISE NOTICE 'Cliente: joao@email.com / admin123';
    RAISE NOTICE '============================================';
END $$;
