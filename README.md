# MinhaVenda - E-commerce Platform

Sistema de e-commerce completo desenvolvido com foco em Clean Architecture e  Domain-Driven Design.

---


## Deploy em Produção

- **Frontend**: https://minhavenda-frontend.vercel.app
- **Backend API**: https://minhavenda-production.up.railway.app/api

## Stack 

### Backend
- **Java 17** - 
- **Spring Boot 3.2.1** - Framework enterprise para microsserviços
- **Spring Security** - Autenticação e autorização com JWT
- **Spring Data JPA** - Persistência de dados com Hibernate para desenvolvimento
- **PostgreSQL** - Banco de dados relacional em produção
- **Flyway** - Versionamento de banco de dados
- **SpringDoc OpenAPI** - Documentação automática da API

### Frontend
- **React 19** - Biblioteca com React Server Components
- **Vite** - Build tool  com HMR
- **Tailwind CSS** - Framework CSS utility-first
- **React Router v7** - Roteamento client-side
- **React Hook Form** - Formulários performáticos
- **Context API** - Gerenciamento de estado global
- **Axios** - Cliente HTTP para requisições

### Ferramentas e infraestrutura
- **Lombok** - Redução de boilerplate
- **MapStruct** - Mapeamento entre Entity e DTO
- **Maven** - Gerenciamento de dependências
- **Vercel** - Hosting frontend com CDN global
- **Railway** - Backend-as-a-Service com PostgreSQL

---

## Arquitetura

```
                     ┌─────────────────────────────────────┐
                     │         Presentation Layer          │
                     │        (Controllers REST)           │
                     └─────────────────────────────────────┘
                                      ↓
                     ┌─────────────────────────────────────┐
                     │         Application Layer           │
                     │   (Use Cases / DTOs / Mappers)      │
                     └─────────────────────────────────────┘
                                      ↓
                     ┌─────────────────────────────────────┐
                     │            Domain Layer             │
                     │  (Entities / Value Objects / Regras)│
                     └─────────────────────────────────────┘
                                      ↓
                     ┌─────────────────────────────────────┐
                     │        Infrastructure Layer         │
                     │   (Repositories / Database / Impl.) │
                     └─────────────────────────────────────┘
```
### Organização dos Pacotes

```
src/main/java/br/com/minhavenda/minhavenda/
│
├── presentation/                          # Camada de Apresentação
│   └── controller/                        # Controllers REST
│       ├── ProdutoController.java
│       ├── PedidoController.java
│       ├── UsuarioController.java
│       └── AuthenticationController.java
│
├── application/                           # Camada de Aplicação
│   ├── dto/                              # Data Transfer Objects
│   │   ├── ProdutoDTO.java
│   │   ├── PedidoDTO.java
│   │   └── AuthenticationResponse.java
│   │
│   ├── mapper/                           # Conversores Entity ↔ DTO
│   │   ├── ProdutoMapper.java
│   │   └── PedidoMapper.java
│   │
│   └── usecase/                          # Casos de Uso
│       ├── produto/                      # Use Cases de Produtos
│       ├── pedido/                       # Use Cases de Pedidos
│       ├── usuario/                      # Use Cases de Usuários
│       └── auth/                         # Use Cases de Autenticação
│
├── domain/                               # Camada de Domínio
│   ├── entity/                          # Entidades de Negócio
│   │   ├── Produto.java
│   │   ├── Pedido.java
│   │   ├── Usuario.java
│   │   ├── Carrinho.java
│   │   ├── Categoria.java
│   │   └── Estoque.java
│   │
│   ├── valueobject/                     # Value Objects
│   │   ├── Money.java
│   │   └── Email.java
│   │
│   └── enums/                           # Enums do Domínio
│       ├── StatusPedido.java
│       ├── TipoUsuario.java
│       └── StatusCarrinho.java
│
├── infrastructure/                       # Camada de Infraestrutura
│   ├── persistence/
│   │   ├── repository/                  # Interfaces Repository
│   │   └── specification/               # Critérios de Busca
│   │
│   └── security/                        # Configurações de Segurança
│       ├── JwtService.java
│       ├── JwtAuthenticationFilter.java
│       └── CustomUserDetailsService.java
│
└── config/                              # Configurações Globais
    ├── SecurityConfig.java
    ├── GlobalExceptionHandler.java
    └── OpenApiConfig.java
```

---

## Funcionalidades Implementadas

### Core Business
- **Catálogo de Produtos**: Listagem, busca avançada e filtros dinâmicos
- **Gestão de Usuários**: Registro, autenticação e perfil
- **Carrinho de Compras**: Gerenciamento persistente de itens
- **Processo de Checkout**: Completo com cálculo de frete
- **Gestão de Pedidos**: Criação, acompanhamento e histórico
- **Controle de Estoque**: Atualização em tempo real
- **Sistema de Categorias**: Organização hierárquica

### Features Técnicas
- **Autenticação JWT**: Stateless com refresh tokens
- **Autorização RBAC**: Role-based access control (ADMIN/CLIENTE)
- **Documentação API**: Swagger/OpenAPI 3.0 automática
- **Validações**: Jakarta Validation em todas as camadas
- **Monitoramento**: Spring Actuator + métricas Prometheus
- **Tratamento de Erros**: Global exception handler
- **Versionamento API**: Controle de versão por URL

---

## Como Rodar Localmente

### Pré-requisitos
- Java 17+
- Node.js 18+
- PostgreSQL 14+ (opcional, H2 disponível)
- Maven 3.8+

### Backend Setup

1. **Clone o repositório**
```bash
git clone https://github.com/rodrigosantoscosta/minhavenda.git
cd minhavenda
```

2. **Configure o ambiente**
```bash
# Para desenvolvimento com H2 (recomendado)
mvn spring-boot:run

# Para PostgreSQL
cp .env.example .env
# Configure suas credenciais no .env
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

3. **Acesse os serviços**
- **API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console (dev)

### Frontend Setup

1. **Navegue para o diretório frontend**
```bash
cd minhavenda-frontend
```

2. **Instale as dependências**
```bash
npm install
```

3. **Configure as variáveis de ambiente**
```bash
cp .env.example .env
# Configure a URL da API backend
```

4. **Inicie o servidor de desenvolvimento**
```bash
npm run dev
```

5. **Acesse a aplicação**
- **Frontend**: http://localhost:5173
- **Build para produção**: `npm run build`

### Docker Setup (Opcional)
```bash
# Inicie PostgreSQL 
docker-compose up -d

# Para desenvolvimento completo
docker-compose -f docker-compose.dev.yml up --build
```

---

## Documentação da API

### Endpoints Principais

#### Autenticação
- `POST /api/auth/login` - Login de usuário
- `POST /api/auth/register` - Registro de novo usuário
- `POST /api/auth/refresh` - Refresh token JWT

#### Produtos
- `GET /api/produtos` - Listar produtos com paginação
- `GET /api/produtos/{id}` - Buscar produto por ID
- `GET /api/produtos/buscar` - Busca com filtros avançados
- `POST /api/produtos` - Criar novo produto (admin)

#### Pedidos
- `GET /api/pedidos` - Listar pedidos do usuário
- `GET /api/pedidos/{id}` - Detalhes do pedido
- `POST /api/pedidos/checkout` - Finalizar pedido
- `PATCH /api/pedidos/{id}/status` - Atualizar status

### Documentação Completa
Acesse a documentação interativa: http://localhost:8080/api/swagger-ui.html

---

## Banco de Dados

### Schema Principal
- **usuarios**: Gestão de usuários e roles
- **categorias**: Categorias de produtos
- **produtos**: Catálogo de produtos com atributos
- **estoques**: Controle de quantidade por produto
- **carrinhos**: Carrinhos de compra dos usuários
- **itens_carrinho**: Items nos carrinhos
- **pedidos**: Pedidos realizados
- **itens_pedido**: Composição dos pedidos
- **pagamentos**: Informações de pagamento
- **entregas**: Dados de entrega
- **notificacoes**: Sistema de notificações

### Migrations
Versionamento automático com Flyway:
- `V1__create_tables.sql` - Estrutura base
- `V2__create_indexes.sql` - Índices de performance
- `V3__insert_categorias.sql` - Dados iniciais
- `V4__insert_usuarios.sql` - Usuários de exemplo
- `V5__insert_produtos.sql` - Produtos de demonstração
- `V6__insert_estoques.sql` - Estoques iniciais

---

## Padrões e práticas

### Clean Architecture
- Separação estrita de responsabilidades
- Dependências apontam para o centro (Domain)
- Business rules isoladas em entities
- Use Cases orquestram fluxos de negócio

### Domain-Driven Design
- Entidades ricas com comportamentos
- Value Objects para conceitos do domínio
- Repositories abstraem persistência
- Domain Events para desacoplamento

### SOLID Principles
- **S**: Single Responsibility Principle
- **O**: Open/Closed Principle  
- **L**: Liskov Substitution Principle
- **I**: Interface Segregation Principle
- **D**: Dependency Inversion Principle

### Performance
- Lazy loading em relações JPA
- Índices otimizados em tabelas críticas
- Connection pooling com HikariCP
- Cache de consultas frequentes

### Segurança
- JWT com expiração configurável
- BCrypt para hash de senhas
- CORS configurado para produção
- Input validation em todas as camadas

---

## Build e Deploy

### Build Backend
```bash
# Build completo
mvn clean package

# Pular testes (CI/CD)
mvn clean package -DskipTests

# Docker image
docker build -t minhavenda-backend .
```

### Build Frontend
```bash
cd minhavenda-frontend

# Development
npm run dev

# Production build
npm run build

# Linting
npm run lint
```

### Deploy em Produção
- **Frontend**: Deploy automático no Vercel via GitHub Actions
- **Backend**: Railway com PostgreSQL gerenciado
- **Monitoramento**: Logs centralizados e métricas em tempo real

---

### Health Checks
- `/actuator/health` - Status geral
- `/actuator/health/db` - Conexão com DB
- `/actuator/health/diskSpace` - Espaço em disco

---

## Roadmap Futuro

### Correções e melhorias
- Correções de bugs
- Melhorias de performance
- Melhorias de segurança
- Atualizações em UX e UI
- Implementação de teste com JUnit
- Mensageria com RabbitMQ

### Features
- Integração com gateway de pagamento
- Sistema de avaliações de produtos
- Sistema de notificações
- Painel administrativo completo
- Painel de gestão de estoque
---

## Licença

Este projeto está licenciado sob a MIT License.

---
