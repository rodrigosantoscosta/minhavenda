-- ========================================
-- V6 - INSERT ESTOQUES
-- Criação de estoque para todos os produtos
-- ========================================

-- Inserir estoque usando subquery para pegar os IDs dos produtos
INSERT INTO estoques (produto_id, quantidade)
SELECT id, 50 FROM produtos WHERE nome LIKE 'Smartphone Samsung%'
UNION ALL
SELECT id, 30 FROM produtos WHERE nome LIKE 'Notebook Dell%'
UNION ALL
SELECT id, 100 FROM produtos WHERE nome LIKE 'Fone de Ouvido%'
UNION ALL
SELECT id, 25 FROM produtos WHERE nome LIKE 'Smart TV LG%'
UNION ALL
SELECT id, 75 FROM produtos WHERE nome LIKE 'Mouse Gamer%'
UNION ALL
SELECT id, 200 FROM produtos WHERE nome LIKE 'Camiseta Básica%'
UNION ALL
SELECT id, 150 FROM produtos WHERE nome LIKE 'Calça Jeans%'
UNION ALL
SELECT id, 80 FROM produtos WHERE nome LIKE 'Vestido Floral%'
UNION ALL
SELECT id, 60 FROM produtos WHERE nome LIKE 'Jaqueta Corta-Vento%'
UNION ALL
SELECT id, 100 FROM produtos WHERE nome LIKE 'Tênis Esportivo%'
UNION ALL
SELECT id, 40 FROM produtos WHERE nome LIKE 'Clean Code%'
UNION ALL
SELECT id, 50 FROM produtos WHERE nome LIKE 'O Senhor dos Anéis%'
UNION ALL
SELECT id, 60 FROM produtos WHERE nome LIKE 'Sapiens%'
UNION ALL
SELECT id, 35 FROM produtos WHERE nome LIKE 'Domain-Driven Design%'
UNION ALL
SELECT id, 45 FROM produtos WHERE nome LIKE 'Jogo de Cama%'
UNION ALL
SELECT id, 30 FROM produtos WHERE nome LIKE 'Conjunto de Panelas%'
UNION ALL
SELECT id, 25 FROM produtos WHERE nome LIKE 'Quadro Decorativo%'
UNION ALL
SELECT id, 55 FROM produtos WHERE nome LIKE 'Luminária de Mesa%'
UNION ALL
SELECT id, 70 FROM produtos WHERE nome LIKE 'Bola de Futebol%'
UNION ALL
SELECT id, 40 FROM produtos WHERE nome LIKE 'Halteres%'
UNION ALL
SELECT id, 85 FROM produtos WHERE nome LIKE 'Tapete de Yoga%'
UNION ALL
SELECT id, 15 FROM produtos WHERE nome LIKE 'Bicicleta Ergométrica%'
UNION ALL
SELECT id, 150 FROM produtos WHERE nome LIKE 'Shampoo Pantene%'
UNION ALL
SELECT id, 40 FROM produtos WHERE nome LIKE 'Perfume Importado%'
UNION ALL
SELECT id, 60 FROM produtos WHERE nome LIKE 'Kit Maquiagem%'
UNION ALL
SELECT id, 100 FROM produtos WHERE nome LIKE 'Café Especial%'
UNION ALL
SELECT id, 120 FROM produtos WHERE nome LIKE 'Chocolate Belga%'
UNION ALL
SELECT id, 80 FROM produtos WHERE nome LIKE 'Azeite Extra Virgem%'
UNION ALL
SELECT id, 20 FROM produtos WHERE nome LIKE 'LEGO Star Wars%'
UNION ALL
SELECT id, 30 FROM produtos WHERE nome LIKE 'Boneca Barbie%'
UNION ALL
SELECT id, 100 FROM produtos WHERE nome LIKE 'Carrinho Hot Wheels%'
UNION ALL
SELECT id, 200 FROM produtos WHERE nome LIKE 'Óleo Motor%'
UNION ALL
SELECT id, 50 FROM produtos WHERE nome LIKE 'Pneu Aro%'
UNION ALL
SELECT id, 75 FROM produtos WHERE nome LIKE 'Kit Limpeza Automotiva%'
UNION ALL
SELECT id, 150 FROM produtos WHERE nome LIKE 'Caderno Universitário%'
UNION ALL
SELECT id, 500 FROM produtos WHERE nome LIKE 'Caneta Esferográfica%'
UNION ALL
SELECT id, 80 FROM produtos WHERE nome LIKE 'Mochila Escolar%'
UNION ALL
SELECT id, 60 FROM produtos WHERE nome LIKE 'Calculadora Científica%';

-- ========================================
-- RESULTADO: 40 registros de estoque criados
-- ========================================