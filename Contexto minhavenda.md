# 🎯 CONTEXTO: PROJETO MINHAVENDA - E-COMMERCE FULLSTACK

## 📋 VISÃO GERAL
Projeto de e-commerce completo com backend Spring Boot (DDD) e frontend React (Vite + Tailwind).

**Base URL:** `http://localhost:8080/api`  
**Swagger UI:** `http://localhost:8080/api/swagger-ui.html`

---

## 🏗️ BACKEND - SPRING BOOT (DDD/Clean Architecture)

### **Tecnologias:**
- Spring Boot 3.x
- PostgreSQL
- JWT Authentication
- Flyway Migrations
- Lombok
- Jakarta Validation
- OpenAPI/Swagger

### **Arquitetura DDD:**
```
com.minhavenda/
├── domain/
│   ├── entity/           (Produto, Categoria, Usuario, Carrinho, Pedido, Estoque)
│   ├── repository/       (Interfaces - contracts)
│   └── valueobject/      (Money, Email)
│
├── application/
│   ├── usecase/          (Use Cases - lógica de negócio)
│   │   ├── produto/
│   │   ├── categoria/
│   │   ├── estoque/
│   │   ├── carrinho/
│   │   └── pedido/
│   ├── controller/       (REST Controllers)
│   ├── dto/              (Request/Response DTOs)
│   └── mapper/           (Entity <-> DTO)
│
└── infrastructure/
    ├── security/         (SecurityConfig, JWT)
    └── persistence/      (JPA Repositories - implementações)
```

---

## 🗂️ ENTIDADES PRINCIPAIS

### **Produto:**
```java
@Entity
public class Produto {
    private UUID id;              // PK - UUID
    private String nome;
    private String descricao;
    private Money preco;          // Value Object
    private Boolean ativo;
    private Instant dataCadastro;
    private Categoria categoria;  // ManyToOne
    private Estoque estoque;      // OneToOne
}
```

### **Estoque:**
```java
@Entity
public class Estoque {
    private Long id;              // PK - Long (não UUID!)
    private Produto produto;      // OneToOne
    private Integer quantidade;
    private Instant atualizadoEm;
    
    // Métodos de negócio
    public void adicionar(Integer qtd);
    public void remover(Integer qtd);
    public void ajustar(Integer qtd);
    public boolean temEstoqueSuficiente(Integer qtd);
}
```

### **Categoria:**
```java
@Entity
public class Categoria {
    private Long id;              // PK - Long (não UUID!)
    private String nome;
    private String descricao;
    private Boolean ativa;
    private Instant dataCadastro;
}
```

### **Usuario:**
```java
@Entity
public class Usuario {
    private UUID id;
    private String nome;
    private Email email;          // Value Object
    private String senha;         // BCrypt
    private TipoUsuario tipo;     // ADMIN, CLIENTE
    private Boolean ativo;
}
```

### **Carrinho:**
```java
@Entity
public class Carrinho {
    private UUID id;
    private Usuario usuario;      // ManyToOne
    private StatusCarrinho status; // ATIVO, FINALIZADO, ABANDONADO
    private List<ItemCarrinho> itens; // OneToMany
    private BigDecimal valorTotal;
}
```

### **Pedido:**
```java
@Entity
public class Pedido {
    private UUID id;
    private Usuario usuario;      // ManyToOne
    private StatusPedido status;  // CRIADO, PAGO, ENVIADO, ENTREGUE, CANCELADO
    private List<ItemPedido> itens; // OneToMany
    private BigDecimal subtotal;
    private BigDecimal valorFrete;
    private BigDecimal valorDesconto;
    private BigDecimal valorTotal;
    private String enderecoEntrega;
    private Instant dataCriacao;
    private Instant dataPagamento;
    private Instant dataEnvio;
    private Instant dataEntrega;
}
```

---

## 🌐 API ENDPOINTS (DOCUMENTAÇÃO SWAGGER)

### **🔐 Autenticação**

#### **POST /api/auth/login**
Login de usuário
```json
Request:
{
  "email": "string",
  "senha": "string"
}

Response:
{
  "token": "string",
  "type": "string",
  "email": "string",
  "nome": "string"
}
```

#### **POST /api/auth/register**
Cadastro de novo usuário
```json
Request:
{
  "nome": "string (3-100 chars)",
  "email": "string",
  "senha": "string (min 6 chars)"
}

Response: AuthenticationResponse
```

---

### **👤 Perfil**

#### **GET /api/perfil** 🔒
Buscar perfil do usuário logado
```json
Response:
{
  "id": "uuid",
  "nome": "string",
  "email": "string",
  "tipo": "ADMIN | CLIENTE",
  "ativo": boolean,
  "dataCadastro": "datetime"
}
```

---

### **📦 Produtos**

#### **GET /api/produtos**
Listar produtos ativos
```json
Response: ProdutoDTO[]
```

#### **GET /api/produtos/paginado**
Listar produtos com paginação
```
Query Params:
- page: int (default: 0)
- size: int (default: 20)
- sort: string (default: "nome:asc")
  Exemplos: "preco:asc", "preco:desc", "nome:asc"

Response: Page<ProdutoDTO>
```

#### **GET /api/produtos/buscar**
Buscar produtos com filtros avançados
```
Query Params:
- termo: string (busca no nome/descrição)
- categoriaId: long
- precoMin: number
- precoMax: number
- ativo: boolean (default: true)
- sort: string (default: "nome:asc")
- page: int (default: 0)
- size: int (default: 20)

Response: Page<ProdutoDTO>
```

#### **GET /api/produtos/busca**
Buscar produtos por termo
```
Query Params:
- q: string (required)
- page: int (default: 0)
- size: int (default: 20)
- sort: string (default: "nome:asc")

Response: Page<ProdutoDTO>
```

#### **GET /api/produtos/{id}**
Buscar produto por ID
```
Path Params:
- id: uuid

Response: ProdutoDTO
```

#### **GET /api/produtos/todos** 🔒 ADMIN
Listar todos produtos (incluindo inativos)
```json
Response: ProdutoDTO[]
```

#### **GET /api/produtos/todos/paginado** 🔒 ADMIN
Listar todos produtos paginado
```
Query Params:
- page, size, sort

Response: Page<ProdutoDTO>
```

#### **POST /api/produtos** 🔒 ADMIN
Criar novo produto
```json
Request: ProdutoDTO
Response: ProdutoDTO
```

---

### **🏷️ Categorias**

#### **GET /api/categorias**
Listar categorias ativas
```json
Response: CategoriaDTO[]
```

#### **GET /api/categorias/paginado**
Listar categorias com paginação
```
Query Params:
- page: int (default: 0)
- size: int (default: 20)
- sort: string (default: "nome")
- direction: string (default: "asc")

Response: Page<CategoriaDTO>
```

#### **GET /api/categorias/todas**
Listar todas categorias (incluindo inativas)
```json
Response: CategoriaDTO[]
```

#### **GET /api/categorias/todas/paginado**
Listar todas categorias paginado
```
Query Params:
- page, size, sort

Response: Page<CategoriaDTO>
```

#### **GET /api/categorias/{id}**
Buscar categoria por ID
```
Path Params:
- id: long

Response: CategoriaDTO
```

#### **POST /api/categorias** 🔒 ADMIN
Criar categoria
```json
Request: CategoriaDTO
Response: CategoriaDTO
```

#### **PUT /api/categorias/{id}** 🔒 ADMIN
Atualizar categoria
```json
Request: AtualizarCategoriaRequest
Response: CategoriaDTO
```

#### **DELETE /api/categorias/{id}** 🔒 ADMIN
Excluir categoria

---

### **📦 Estoque**

#### **GET /api/estoque/produto/{produtoId}**
Consultar estoque de um produto
```
Path Params:
- produtoId: uuid

Response:
{
  "produtoId": "uuid",
  "quantidade": int
}
```

#### **POST /api/estoque/produto/{produtoId}/adicionar** 🔒 ADMIN
Adicionar estoque
```json
Request:
{
  "quantidade": int (min: 1),
  "motivo": "string (opcional)"
}

Response:
{
  "mensagem": "string"
}
```

#### **POST /api/estoque/produto/{produtoId}/remover** 🔒 ADMIN
Remover estoque
```json
Request:
{
  "quantidade": int (min: 1),
  "motivo": "string (opcional)"
}

Response:
{
  "mensagem": "string"
}
```

---

### **🛒 Carrinho**

#### **GET /api/carrinho** 🔒
Buscar carrinho do usuário logado
```json
Response: CarrinhoDTO
```

#### **POST /api/carrinho/itens** 🔒
Adicionar item ao carrinho
```json
Request:
{
  "produtoId": "uuid",
  "quantidade": int (min: 1)
}

Response: CarrinhoDTO
```

#### **PUT /api/carrinho/itens/{itemId}** 🔒
Atualizar quantidade do item
```json
Request:
{
  "quantidade": int (min: 1)
}

Response: CarrinhoDTO
```

#### **DELETE /api/carrinho/itens/{itemId}** 🔒
Remover item do carrinho
```
Response: CarrinhoDTO
```

#### **DELETE /api/carrinho** 🔒
Limpar carrinho (remover todos itens)
```
Response: CarrinhoDTO
```

---

### **📋 Pedidos**

#### **POST /api/checkout/finalizar** 🔒
Finalizar checkout (converter carrinho em pedido)
```json
Request:
{
  "enderecoEntrega": "string (required, max 500)",
  "observacoes": "string (opcional, max 1000)"
}

Response: PedidoDTO
```

#### **GET /api/meus-pedidos** 🔒
Listar pedidos do usuário logado
```json
Response: PedidoDTO[]
```

#### **GET /api/pedidos/{id}** 🔒
Buscar pedido específico
```
Path Params:
- id: uuid

Response: PedidoDetalhadoDTO
```

#### **POST /api/pedidos/{id}/pagar** 🔒
Pagar pedido (simulação)
```
Response: PedidoDTO
```

#### **POST /api/pedidos/{id}/cancelar** 🔒
Cancelar pedido
```
Response: PedidoDTO
```

#### **GET /api/admin/pedidos** 🔒 ADMIN
Listar pedidos por status
```
Query Params:
- status: CRIADO | PAGO | ENVIADO | ENTREGUE | CANCELADO (opcional)

Response: PedidoDTO[]
```

#### **GET /api/admin/pedidos/{id}** 🔒 ADMIN
Buscar qualquer pedido
```
Response: PedidoDetalhadoDTO
```

#### **POST /api/pedidos/{id}/enviar** 🔒 ADMIN
Marcar pedido como enviado
```
Response: PedidoDTO
```

#### **POST /api/pedidos/{id}/entregar** 🔒 ADMIN
Marcar pedido como entregue
```
Response: PedidoDTO
```

---

## 📊 DTOs PRINCIPAIS

### **ProdutoDTO:**
```typescript
{
  id: uuid,
  nome: string,
  descricao: string,
  preco: {
    valor: number,
    moeda: string
  },
  ativo: boolean,
  dataCadastro: datetime,
  categoriaId: long,
  categoriaNome: string,
  quantidadeEstoque: int
}
```

### **CarrinhoDTO:**
```typescript
{
  id: uuid,
  usuarioId: uuid,
  status: "ATIVO" | "FINALIZADO" | "ABANDONADO",
  itens: ItemCarrinhoDTO[],
  valorTotal: number,
  quantidadeTotal: int,
  dataCriacao: datetime,
  dataAtualizacao: datetime
}
```

### **PedidoDTO:**
```typescript
{
  id: uuid,
  status: "CRIADO" | "PAGO" | "ENVIADO" | "ENTREGUE" | "CANCELADO",
  subtotal: number,
  valorFrete: number,
  valorDesconto: number,
  valorTotal: number,
  enderecoEntrega: string,
  observacoes: string,
  quantidadeItens: int,
  dataCriacao: datetime,
  dataPagamento: datetime,
  dataEnvio: datetime,
  dataEntrega: datetime
}
```

### **PedidoDetalhadoDTO:**
```typescript
// Extends PedidoDTO + adiciona:
{
  itens: ItemPedidoDTO[]
}
```

---

## 🔐 SEGURANÇA

### **Endpoints Públicos:**
- `/auth/**` (login, register)
- `GET /produtos/**`
- `GET /categorias/**`
- `GET /estoque/**`

### **Endpoints Autenticados:**
- `/perfil/**`
- `/carrinho/**`
- `/pedidos/**`
- `/meus-pedidos`
- `/checkout/**`

### **Endpoints Admin:**
- `POST/PUT/DELETE /produtos/**`
- `POST/PUT/DELETE /categorias/**`
- `POST /estoque/**`
- `/admin/**`

---

## 🎨 FRONTEND - REACT

### **Estrutura:**
```
src/
├── pages/         (Home, Products, ProductDetail, Cart, etc)
├── components/    (Button, ProductCard, Header, etc)
├── contexts/      (AuthContext, CartContext, Toast)
└── services/      (api.js, authService.js, productService.js)
```

### **API Base URL:**
```javascript
const api = axios.create({
  baseURL: 'http://localhost:8080/api'  // ⚠️ /api é obrigatório!
})
```

---

## 🚨 PONTOS CRÍTICOS

### **1. IDs:**
```java
UUID id;   // Produto, Usuario, Carrinho, Pedido
Long id;   // Categoria, Estoque
```

### **2. Base URL:**
```
✅ http://localhost:8080/api
❌ http://localhost:8080
```

### **3. Estoque:**
```java
// ✅ Entidade separada
Estoque estoque = estoqueRepository.findByProduto(produto)

// ❌ Não é campo do produto
produto.setEstoque(10) // NÃO EXISTE!
```

### **4. Sort Format:**
```
✅ sort=preco:asc
✅ sort=nome:desc
❌ sort=preco&direction=asc
```

### **5. Use Cases:**
```java
// ✅ Um Use Case = Uma responsabilidade
AdicionarEstoqueUseCase.executar()

// ❌ Não usar Service genérico
EstoqueService.adicionar()
```

---

## 🧪 TESTAR

```bash
# Backend
mvn spring-boot:run
http://localhost:8080/swagger-ui.html

# Frontend
npm run dev
http://localhost:5173

# Login Admin
admin@minhavenda.com / admin123
```

---
