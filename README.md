# 🛒 MinhaVenda E-Commerce

Sistema de E-Commerce desenvolvido com **Clean Architecture**, **DDD** e **Spring Boot**.

---

## 🚀 Tecnologias

### Backend
- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Data JPA**
- **Spring Security**
- **Maven 3.8+**

### Banco de Dados
- **PostgreSQL** (Produção)
- **H2** (Desenvolvimento/Testes)

### Documentação
- **SpringDoc OpenAPI** (Swagger)

### Ferramentas
- **Lombok** (Redução de boilerplate)
- **MapStruct** (Mapeamento DTO ↔ Entity)
- **Docker & Docker Compose**

---

## 📋 Pré-requisitos

- **Java 17+**
- **Maven 3.8+**
- **Docker & Docker Compose** (opcional)
- **Git**
- **IDE**: IntelliJ IDEA (recomendado) ou VS Code

---

## 🐳 Quick Start

### 1. Clone o repositório
```bash
git clone https://github.com/seu-usuario/minhavenda.git
cd minhavenda
```

### 2. Configure o ambiente (Opcional - para PostgreSQL)
```bash
cp .env.example .env
# Edite .env com suas credenciais
```

### 3. Execute com H2 (Desenvolvimento)
```bash
# Banco em memória - mais rápido para começar
mvn spring-boot:run
```

### 4. Acesse a aplicação
- **API**: http://localhost:8080
- **Swagger**: http://localhost:8080/api/swagger-ui.html


---

## 🏗️ Arquitetura

Este projeto segue os princípios de **Clean Architecture** e **Domain-Driven Design (DDD)**.

### 📐 Camadas

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│         (Controllers REST)              │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Application Layer               │
│    (Use Cases / Casos de Uso)          │
│         (DTOs / Mappers)                │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          Domain Layer                   │
│   (Entities / Value Objects)           │
│      (Regras de Negócio)                │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Infrastructure Layer               │
│   (Repositories / Database)             │
│      (Implementações)                    │
└─────────────────────────────────────────┘
```

### 📂 Estrutura de Diretórios

```
src/main/java/br/com/minhavenda/minhavenda/
│
├── 📱 presentation/                    # Camada de Apresentação
│   └── controller/                     # Controllers REST
│       └── ProdutoController.java      # Endpoints de Produtos
│
├── 🎯 application/                     # Camada de Aplicação
│   ├── dto/                            # Data Transfer Objects
│   │   ├── ProdutoDTO.java
│   │   └── CriarProdutoRequest.java
│   │
│   ├── mapper/                         # Conversores Entity ↔ DTO
│   │   └── ProdutoMapper.java
│   │
│   └── usecase/                        # Casos de Uso (Lógica Aplicação)
│       ├── ListarProdutosUseCase.java
│       ├── BuscarProdutoPorIdUseCase.java
│       └── CriarProdutoUseCase.java
│
├── 🏛️ domain/                          # Camada de Domínio
│   ├── entity/                         # Entidades JPA
│   │   ├── Produto.java
│   │   ├── Categoria.java
│   │   ├── Usuario.java
│   │   ├── Pedido.java
│   │   └── ...
│   │
│   └── valueobject/                    # Value Objects
│       └── Money.java (futuro)
│
├── 🔧 infrastructure/                  # Camada de Infraestrutura
│   └── persistence/
│       └── repository/                 # Repositories (Acesso DB)
│           ├── ProdutoRepository.java
│           └── CategoriaRepository.java
│
└── ⚙️ config/                          # Configurações
    └── SecurityConfig.java             # Spring Security
```

---

## 📊 Modelo de Dados

### Entidades Principais

```
┌─────────────┐       ┌──────────────┐
│  Categoria  │◄──────│   Produto    │
└─────────────┘ 1   N └──────────────┘
                             │
                             │ N
                             │
                      ┌──────▼──────┐
                      │  ItemPedido │
                      └──────┬──────┘
                             │ N
                             │
                      ┌──────▼──────┐
                      │    Pedido   │
                      └──────┬──────┘
                             │ N
                             │
                      ┌──────▼──────┐
                      │   Usuario   │
                      └─────────────┘
```

---

## 🔌 Endpoints Disponíveis

### 📦 Produtos

| Método | Endpoint                  | Descrição                        |
|--------|---------------------------|----------------------------------|
| GET    | `/produtos`               | Lista produtos ativos            |
| GET    | `/produtos/paginado`      | Lista com paginação              |
| GET    | `/produtos/{id}`          | Busca produto por ID             |
| GET    | `/produtos/todos`         | Lista todos (ativos + inativos)  |
| POST   | `/produtos`               | Cria novo produto                |

### 🔍 Exemplos de Uso

**Listar produtos:**
```bash
GET http://localhost:8080/produtos
```

**Listar com paginação:**
```bash
GET http://localhost:8080/produtos/paginado?page=0&size=20&sort=preco&direction=asc
```

**Buscar por ID:**
```bash
GET http://localhost:8080/produtos/550e8400-e29b-41d4-a716-446655440001
```

**Criar produto:**
```bash
POST http://localhost:8080/produtos
Content-Type: application/json

{
  "nome": "Notebook Dell",
  "descricao": "Core i7, 16GB RAM",
  "preco": 3500.00,
  "categoriaId": 1,
  "ativo": true
}
```

---

## 📚 Documentação API

### Swagger UI
Acesse a documentação interativa completa:

```
http://localhost:8080/api/swagger-ui.html
```

### OpenAPI JSON
```
http://localhost:8080/api-docs
```

---

## 🧪 Testes

### Executar testes
```bash
# Testes unitários
mvn test

# Testes de integração
mvn verify

# Ver cobertura
mvn jacoco:report
```

### Testar com Insomnia/Postman

1. Importe a collection: `minhavenda-export-ATUALIZADO.json`
2. Configure base URL: `http://localhost:8080`
3. Execute os requests

---

## 📦 Build & Deploy

### Compilar
```bash
# Build completo
mvn clean package

# Pular testes
mvn clean package -DskipTests
```

### Executar JAR
```bash
java -jar target/minhavenda-1.0.0.jar
```

---

## 🗄️ Banco de Dados

### PostgreSQL (Produção)
```bash
# Conectar
psql -h localhost -U postgres -d minhavenda

# Ver tabelas
\dt

# Ver dados
SELECT * FROM produtos;
```

### Dados de Teste
```bash
# Executar script SQL
# Via H2 Console: copie e cole dados-teste.sql
# Via PostgreSQL: psql -f dados-teste.sql
```

---

## 🔐 Segurança

### Configuração Atual
- **Desenvolvimento**: Autenticação desabilitada (facilitar testes)
- **Produção**: JWT + Spring Security (implementar futuramente)

### Variáveis de Ambiente
```bash
# .env
POSTGRES_PASSWORD=sua-senha
JWT_SECRET=sua-chave-secreta
```

⚠️ **NUNCA commite o arquivo `.env`!**

---

## 📖 Padrões e Boas Práticas

### Clean Architecture
- ✅ Separação de responsabilidades por camadas
- ✅ Dependências apontam para dentro (Domain no centro)
- ✅ Regras de negócio isoladas na camada Domain

### DDD (Domain-Driven Design)
- ✅ Entidades ricas com comportamento
- ✅ Value Objects para conceitos do domínio
- ✅ Repositories abstraem persistência
- ✅ Use Cases orquestram operações

### SOLID
- ✅ **S**ingle Responsibility: Uma classe, uma responsabilidade
- ✅ **O**pen/Closed: Aberto para extensão, fechado para modificação
- ✅ **L**iskov Substitution: Subtipos substituíveis
- ✅ **I**nterface Segregation: Interfaces específicas
- ✅ **D**ependency Inversion: Dependa de abstrações

---

## 🚧 Roadmap

### ✅ Fase 1 - MVP (Concluído)
- [x] Estrutura do projeto
- [x] Entidades JPA
- [x] Repositories
- [x] Use Cases básicos
- [x] Controllers REST
- [x] Swagger configurado
- [x] Dados de teste

### 🔄 Fase 2 - Autenticação (Em desenvolvimento)
- [ ] JWT Authentication
- [ ] User Registration
- [ ] Login/Logout
- [ ] Roles (USER, ADMIN)

### 📅 Fase 3 - Carrinho e Pedidos
- [ ] Adicionar ao carrinho
- [ ] Finalizar pedido
- [ ] Processar pagamento
- [ ] Histórico de pedidos

### 📅 Fase 4 - Avançado
- [ ] Busca avançada (Elasticsearch)
- [ ] Cache (Redis)
- [ ] Mensageria (RabbitMQ)
- [ ] Upload de imagens
- [ ] Integração com gateway de pagamento

---

### Padrão de Commits
```
feat: nova funcionalidade
fix: correção de bug
docs: documentação
refactor: refatoração
test: testes
chore: tarefas gerais
```
