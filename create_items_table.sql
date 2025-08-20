-- Script de criação da tabela items com os dados atuais
-- Gerado em 20/08/2025

-- Criar tabela items
CREATE TABLE IF NOT EXISTS items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255) NOT NULL,
    value INT NOT NULL,
    category VARCHAR(32) NOT NULL,
    description VARCHAR(255) NULL
);

-- Inserir todos os itens cadastrados
INSERT INTO items (id, name, value, category, description) VALUES
(1, 'Alcatra', 800, 'SKEWER', 'Espetinho de alcatra grelhado'),
(2, 'Filé com Alho', 900, 'SKEWER', 'Filé com alho grelhado'),
(3, 'Medalhão de Frango', 1000, 'SKEWER', 'Medalhão de frango grelhado'),
(4, 'Chopp', 1000, 'DRINK', 'Chopp gelado'),
(5, 'Água', 300, 'DRINK', 'Água mineral'),
(6, 'Refrigerante', 500, 'DRINK', 'Refrigerante gelado'),
(7, 'Batata Frita', 1500, 'SNACK', 'Batata frita crocante'),
(8, 'Medalhão de Alcatra', 1200, 'SKEWER', NULL),
(9, 'Medalhão de Queijo', 1000, 'SKEWER', NULL),
(10, 'Pão de alho', 700, 'SKEWER', NULL),
(11, 'Coração', 700, 'SKEWER', NULL),
(12, 'Toscana', 500, 'SKEWER', NULL),
(13, 'Mamilo', 800, 'SKEWER', NULL),
(14, 'Queijo', 700, 'SKEWER', NULL),
(15, 'Bolinho de Queijo', 1400, 'SNACK', NULL),
(16, 'Coxinha', 1400, 'SNACK', NULL),
(17, 'Bolinho de Charque', 1600, 'SNACK', NULL),
(18, 'Chopp Promo', 800, 'PROMOTIONAL', NULL),
(19, 'Combo quarta', 3000, 'PROMOTIONAL', '2 Chopps + fritas'),
(20, 'Combo quinta', 3200, 'PROMOTIONAL', '2 Chopps + bolinho de charque'),
(21, 'Água de côco', 500, 'DRINK', NULL),
(22, 'Energético', 1200, 'DRINK', NULL);

-- Resetar o contador de auto increment para o próximo ID
UPDATE sqlite_sequence SET seq = 22 WHERE name = 'items';