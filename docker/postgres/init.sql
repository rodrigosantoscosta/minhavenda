-- ===============================
-- MINHAVENDA - PostgreSQL Init Script
-- ===============================

-- Extensões úteis
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm"; -- Para busca full-text

-- Criar schema se necessário
-- CREATE SCHEMA IF NOT EXISTS minhavenda;

-- Comentários no banco
COMMENT ON DATABASE minhavenda_dev IS 'Banco de dados do sistema MinhaVenda E-Commerce';

-- Criar usuário adicional para leitura apenas (útil para analytics)
-- CREATE USER minhavenda_readonly WITH PASSWORD 'readonly_password';
-- GRANT CONNECT ON DATABASE minhavenda_dev TO minhavenda_readonly;
-- GRANT USAGE ON SCHEMA public TO minhavenda_readonly;
-- GRANT SELECT ON ALL TABLES IN SCHEMA public TO minhavenda_readonly;
-- ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO minhavenda_readonly;

-- Log de inicialização
DO $$
BEGIN
    RAISE NOTICE 'Database minhavenda_dev initialized successfully!';
    RAISE NOTICE 'Extensions loaded: uuid-ossp, pg_trgm';
END $$;
