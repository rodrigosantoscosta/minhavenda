-- ================================================
-- SCRIPT DE DADOS DE TESTE - MinhaVenda
-- ================================================

-- ================================================
-- CATEGORIAS
-- ================================================
INSERT INTO categorias (id, nome, descricao, ativo, data_cadastro) VALUES
(1, 'Eletrônicos', 'Notebooks, celulares, tablets e acessórios', true, CURRENT_TIMESTAMP),
(2, 'Informática', 'Componentes, periféricos e hardware', true, CURRENT_TIMESTAMP),
(3, 'Games', 'Consoles, jogos e acessórios para gamers', true, CURRENT_TIMESTAMP),
(4, 'Livros', 'Livros físicos e digitais', true, CURRENT_TIMESTAMP),
(5, 'Casa e Decoração', 'Móveis, decoração e utilidades domésticas', true, CURRENT_TIMESTAMP);

-- ================================================
-- PRODUTOS - ELETRÔNICOS
-- ================================================
INSERT INTO produtos (id, nome, descricao, preco, ativo, data_cadastro, categoria_id) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'Notebook Dell Inspiron 15', 'Intel Core i7-1165G7, 16GB RAM, SSD 512GB, Tela 15.6" Full HD', 3499.00, true, CURRENT_TIMESTAMP, 1),
('550e8400-e29b-41d4-a716-446655440002', 'Smartphone Samsung Galaxy S23', '128GB, 8GB RAM, Câmera 50MP, Tela 6.1" AMOLED', 2899.00, true, CURRENT_TIMESTAMP, 1),
('550e8400-e29b-41d4-a716-446655440003', 'iPhone 15 Pro', '256GB, Chip A17 Pro, Câmera 48MP, Titânio', 7999.00, true, CURRENT_TIMESTAMP, 1),
('550e8400-e29b-41d4-a716-446655440004', 'Tablet Samsung Galaxy Tab S9', '11", 128GB, S Pen inclusa, Android 13', 2199.00, true, CURRENT_TIMESTAMP, 1),
('550e8400-e29b-41d4-a716-446655440005', 'Smart TV LG 55" 4K', 'OLED, WebOS, ThinQ AI, 4 HDMI', 3799.00, true, CURRENT_TIMESTAMP, 1),
('550e8400-e29b-41d4-a716-446655440006', 'Fone Bluetooth Sony WH-1000XM5', 'Cancelamento de ruído, 30h bateria, Hi-Res Audio', 1899.00, true, CURRENT_TIMESTAMP, 1),
('550e8400-e29b-41d4-a716-446655440007', 'Apple Watch Series 9', 'GPS + Cellular, 45mm, Caixa de Alumínio', 4299.00, true, CURRENT_TIMESTAMP, 1),
('550e8400-e29b-41d4-a716-446655440008', 'Câmera Canon EOS R6', 'Full Frame, 20.1MP, 4K 60fps, Kit com Lente 24-105mm', 16999.00, true, CURRENT_TIMESTAMP, 1);

-- ================================================
-- PRODUTOS - INFORMÁTICA
-- ================================================
INSERT INTO produtos (id, nome, descricao, preco, ativo, data_cadastro, categoria_id) VALUES
('550e8400-e29b-41d4-a716-446655440009', 'Mouse Gamer Logitech G502 Hero', 'RGB, 25.600 DPI, 11 botões programáveis, Sensor Hero 25K', 289.90, true, CURRENT_TIMESTAMP, 2),
('550e8400-e29b-41d4-a716-446655440010', 'Teclado Mecânico Keychron K2', 'Switch Gateron Brown, 84 teclas, Bluetooth + USB-C', 599.00, true, CURRENT_TIMESTAMP, 2),
('550e8400-e29b-41d4-a716-446655440011', 'Monitor LG UltraWide 34"', '34WN80C, QHD, IPS, USB-C, HDR10', 2399.00, true, CURRENT_TIMESTAMP, 2),
('550e8400-e29b-41d4-a716-446655440012', 'SSD Kingston NV2 1TB', 'M.2 NVMe PCIe 4.0, Leitura 3500MB/s', 399.00, true, CURRENT_TIMESTAMP, 2),
('550e8400-e29b-41d4-a716-446655440013', 'Memória RAM Corsair Vengeance 32GB', 'DDR4 3200MHz, Kit 2x16GB, RGB Pro', 649.00, true, CURRENT_TIMESTAMP, 2),
('550e8400-e29b-41d4-a716-446655440014', 'Placa de Vídeo RTX 4070', 'NVIDIA GeForce, 12GB GDDR6X, Ray Tracing', 4299.00, true, CURRENT_TIMESTAMP, 2),
('550e8400-e29b-41d4-a716-446655440015', 'Webcam Logitech C920e', 'Full HD 1080p, Microfone estéreo, AutoFocus', 449.00, true, CURRENT_TIMESTAMP, 2),
('550e8400-e29b-41d4-a716-446655440016', 'HD Externo Seagate 2TB', 'USB 3.0, Portátil, Backup Plus Slim', 449.00, true, CURRENT_TIMESTAMP, 2);

-- ================================================
-- PRODUTOS - GAMES
-- ================================================
INSERT INTO produtos (id, nome, descricao, preco, ativo, data_cadastro, categoria_id) VALUES
('550e8400-e29b-41d4-a716-446655440017', 'PlayStation 5', 'Console 825GB SSD, Controle DualSense, 4K 120fps', 3999.00, true, CURRENT_TIMESTAMP, 3),
('550e8400-e29b-41d4-a716-446655440018', 'Xbox Series X', '1TB SSD, 4K HDR, Ray Tracing, Game Pass Ultimate', 3799.00, true, CURRENT_TIMESTAMP, 3),
('550e8400-e29b-41d4-a716-446655440019', 'Nintendo Switch OLED', 'Tela 7" OLED, 64GB, Dock e Joy-Cons', 2399.00, true, CURRENT_TIMESTAMP, 3),
('550e8400-e29b-41d4-a716-446655440020', 'God of War Ragnarök - PS5', 'Mídia física, Legendas em Português', 299.00, true, CURRENT_TIMESTAMP, 3),
('550e8400-e29b-41d4-a716-446655440021', 'The Legend of Zelda TOTK', 'Tears of the Kingdom - Nintendo Switch', 349.00, true, CURRENT_TIMESTAMP, 3),
('550e8400-e29b-41d4-a716-446655440022', 'Controle Xbox Elite Series 2', 'Wireless, Componentes ajustáveis, 40h bateria', 1299.00, true, CURRENT_TIMESTAMP, 3),
('550e8400-e29b-41d4-a716-446655440023', 'Headset Gamer HyperX Cloud II', '7.1 Surround, Driver 53mm, Microfone removível', 549.00, true, CURRENT_TIMESTAMP, 3),
('550e8400-e29b-41d4-a716-446655440024', 'Cadeira Gamer DXRacer Formula', 'Ergonômica, Reclinável 135°, Suporta 150kg', 1899.00, true, CURRENT_TIMESTAMP, 3);

-- ================================================
-- PRODUTOS - LIVROS
-- ================================================
INSERT INTO produtos (id, nome, descricao, preco, ativo, data_cadastro, categoria_id) VALUES
('550e8400-e29b-41d4-a716-446655440025', 'Clean Code - Robert C. Martin', 'Habilidades Práticas do Agile Software', 89.90, true, CURRENT_TIMESTAMP, 4),
('550e8400-e29b-41d4-a716-446655440026', 'Domain-Driven Design - Eric Evans', 'Atacando as Complexidades no Coração do Software', 119.00, true, CURRENT_TIMESTAMP, 4),
('550e8400-e29b-41d4-a716-446655440027', 'Arquitetura Limpa - Robert Martin', 'O Guia do Artesão para Estrutura e Design', 79.90, true, CURRENT_TIMESTAMP, 4),
('550e8400-e29b-41d4-a716-446655440028', 'Refatoração - Martin Fowler', 'Aperfeiçoando o Design de Códigos Existentes', 99.00, true, CURRENT_TIMESTAMP, 4),
('550e8400-e29b-41d4-a716-446655440029', 'O Programador Pragmático', 'De Aprendiz a Mestre - David Thomas', 89.90, true, CURRENT_TIMESTAMP, 4),
('550e8400-e29b-41d4-a716-446655440030', 'Design Patterns - Gang of Four', 'Elements of Reusable Object-Oriented Software', 139.00, true, CURRENT_TIMESTAMP, 4);

-- ================================================
-- PRODUTOS - CASA E DECORAÇÃO
-- ================================================
INSERT INTO produtos (id, nome, descricao, preco, ativo, data_cadastro, categoria_id) VALUES
('550e8400-e29b-41d4-a716-446655440031', 'Cadeira Ergonômica Premium', 'Escritório, Apoio lombar ajustável, Braços 4D', 1299.00, true, CURRENT_TIMESTAMP, 5),
('550e8400-e29b-41d4-a716-446655440032', 'Mesa Escrivaninha Madesa', '120cm, 2 Gavetas, MDF, Cor Branca', 449.00, true, CURRENT_TIMESTAMP, 5),
('550e8400-e29b-41d4-a716-446655440033', 'Luminária LED de Mesa', 'Articulada, 3 níveis de brilho, USB recarregável', 129.00, true, CURRENT_TIMESTAMP, 5),
('550e8400-e29b-41d4-a716-446655440034', 'Quadro Decorativo Kit 3 Peças', 'Abstrato Geométrico, Canvas 40x60cm', 189.00, true, CURRENT_TIMESTAMP, 5),
('550e8400-e29b-41d4-a716-446655440035', 'Tapete Antiderrapante', '200x140cm, Sala/Quarto, Pelo Baixo', 249.00, true, CURRENT_TIMESTAMP, 5);

-- ================================================
-- PRODUTOS INATIVOS (Para testar filtros)
-- ================================================
INSERT INTO produtos (id, nome, descricao, preco, ativo, data_cadastro, categoria_id) VALUES
('550e8400-e29b-41d4-a716-446655440036', 'iPhone 12 - DESCONTINUADO', '64GB, Preto, Modelo antigo', 2499.00, false, CURRENT_TIMESTAMP, 1),
('550e8400-e29b-41d4-a716-446655440037', 'Mouse Básico - ESGOTADO', 'USB com fio, 1000 DPI', 29.90, false, CURRENT_TIMESTAMP, 2),
('550e8400-e29b-41d4-a716-446655440038', 'PS4 Slim - DESCONTINUADO', '500GB, Usado, Sem garantia', 1299.00, false, CURRENT_TIMESTAMP, 3);

-- ================================================
-- FIM DO SCRIPT
-- ================================================

-- Verificar inserções
SELECT 'Categorias inseridas:' as info, COUNT(*) as total FROM categorias;
SELECT 'Produtos ativos:' as info, COUNT(*) as total FROM produtos WHERE ativo = true;
SELECT 'Produtos inativos:' as info, COUNT(*) as total FROM produtos WHERE ativo = false;
SELECT 'Total de produtos:' as info, COUNT(*) as total FROM produtos;
select * from produtos;

-- Resetar sequence
SELECT setval('categorias_id_seq', (SELECT COALESCE(MAX(id), 0) FROM categorias) + 1);