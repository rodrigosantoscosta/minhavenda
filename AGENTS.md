AI coding agents guide for **MinhaVenda** e-commerce codebase.

MinhaVenda is a full-stack application: Spring Boot backend (Clean Architecture/DDD) + React frontend (Vite).

---

## Build & Commands

### Backend (Spring Boot - Root Directory)

```bash
# Start development server with H2
mvn spring-boot:run

# Build project
mvn clean package

# Run all tests
mvn test

# Run specific test
mvn test -Dtest=PedidoServiceTest

# Run tests with coverage
mvn verify
mvn jacoco:report

# Start with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Generate production JAR
mvn clean package -DskipTests
java -jar target/minhavenda-1.0.0.jar
```


### Frontend (React + Vite - minhavenda-frontend/)

```bash
cd minhavenda-frontend

# Install dependencies
npm install

# Start dev server (http://localhost:5173)
npm run dev

# Build for production
npm run build

# Run linter
npm run lint

# Fix linting issues
npm run lint:fix

# Preview production build
npm run preview
```


### Docker Services

```bash
# Start PostgreSQL and RabbitMQ
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f
```


---

## Development Environment

- **Backend:** http://localhost:8080
- **Frontend:** http://localhost:5173
- **Swagger UI:** http://localhost:8080/api/swagger-ui.html
- **H2 Console:** http://localhost:8080/h2-console (dev only)
    - JDBC URL: `jdbc:h2:mem:testdb`
    - User: `sa`
    - Password: (empty)
- **PostgreSQL:** localhost:5432 (production)
- **RabbitMQ:** localhost:5672 (messaging)

---

## Project Structure

### Backend (Clean Architecture/DDD)

```
src/main/java/br/com/minhavenda/minhavenda/
├── presentation/          # REST Controllers (HTTP layer)
├── application/          # Use Cases, DTOs, Mappers, Events
├── domain/              # Entities, Value Objects, Domain Events, Business Rules
├── infrastructure/      # JPA Repositories, Email Service, External APIs
└── config/             # Spring Security, CORS, Jackson, Flyway
```

**Layer Responsibilities:**

- **Presentation:** Handle HTTP requests/responses, delegate to Use Cases
- **Application:** Orchestrate business logic, publish domain events
- **Domain:** Core business rules, entities, value objects, domain events
- **Infrastructure:** External concerns (database, email, etc.)


### Frontend (React + Vite)

```
minhavenda-frontend/src/
├── components/         # Reusable UI components (Button, Input, Card)
├── pages/             # Route-level components (HomePage, ProductPage)
├── services/          # API service layer (authService, productService)
├── contexts/          # React contexts (AuthContext, CartContext)
├── utils/            # Utility functions (formatters, validators)
└── App.jsx           # Main routing and app setup
```


---

## Code Style

### Backend (Java/Spring Boot)

**Naming Conventions:**

- Classes: `PascalCase` (e.g., `ProdutoController`, `FinalizarCheckoutUseCase`)
- Methods: `camelCase` (e.g., `buscarPorId`, `executar`)
- Variables: `camelCase` (e.g., `produtoId`, `nomeUsuario`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `DEFAULT_PAGE_SIZE`)
- Packages: `lowercase` (e.g., `br.com.minhavenda.minhavenda.domain`)

**Entity Pattern:**

```java
@Entity
@Table(name = "pedidos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    // Business method with validation
    public void pagar(String metodoPagamento) {
        if (this.status != StatusPedido.CRIADO) {
            throw new IllegalStateException("Apenas pedidos CRIADO podem ser pagos");
        }
        this.status = StatusPedido.PAGO;
        this.dataPagamento = Instant.now();
        
        // Emit domain event
        this.registrarEvento(new PedidoPagoEvent(...));
    }
}
```

**Use Case Pattern:**

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class FinalizarCheckoutUseCase {
    
    private final PedidoRepository pedidoRepository;
    private final DomainEventPublisher eventPublisher;
    
    @Transactional
    public Pedido executar(UUID usuarioId, CheckoutRequest request) {
        // 1. Business logic
        Pedido pedido = new Pedido(...);
        
        // 2. Save
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        
        // 3. Register event
        pedidoSalvo.registrarCriacao();
        
        // 4. Publish events
        eventPublisher.publishAll(pedidoSalvo.getDomainEvents());
        pedidoSalvo.limparEventos();
        
        return pedidoSalvo;
    }
}
```

**Controller Pattern:**

```java
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos")
public class PedidoController {
    
    private final FinalizarCheckoutUseCase finalizarCheckoutUseCase;
    
    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PedidoDTO> checkout(@Valid @RequestBody CheckoutRequest request) {
        Pedido pedido = finalizarCheckoutUseCase.executar(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(pedido));
    }
}
```

**Important Patterns:**

- **Entities:** Use `Instant` (UTC) for dates, not `LocalDateTime`
- **DTOs:** Use `LocalDateTime` (convert in mapper using timezone)
- **Domain Events:** Always publish after saving aggregate root
- **Validation:** Use Jakarta Bean Validation (`@NotBlank`, `@Valid`)
- **Error Handling:** Throw `IllegalArgumentException` with clear messages
- **Repositories:** Always use `Optional<T>` for find methods
- **Transactions:** Use `@Transactional` on Use Cases, not Controllers


### Frontend (React/JavaScript)

**Component Pattern:**

```jsx
import { useState } from 'react';
import { Button } from '@/components/ui/Button';

export default function ProductCard({ product, onAddToCart }) {
  const [isLoading, setIsLoading] = useState(false);

  const handleAddToCart = async () => {
    setIsLoading(true);
    try {
      await cartService.addItem(product.id, 1);
      onAddToCart?.();
    } catch (error) {
      // Error handled by global interceptor
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="border rounded-lg p-4 hover:shadow-lg transition-shadow">
      ```
      <h3 className="font-semibold text-lg mb-2">{product.nome}</h3>
      ```
      ```
      <p className="text-primary-600 font-bold">R$ {product.preco}</p>
      ```
      <Button 
        onClick={handleAddToCart}
        disabled={isLoading}
        className="w-full mt-4"
      >
        {isLoading ? 'Adicionando...' : 'Adicionar ao Carrinho'}
      </Button>
    </div>
  );
}
```

**Service Pattern:**

```javascript
// services/productService.js
import { api } from './api';

export const productService = {
  async getProducts(filters = {}) {
    const params = new URLSearchParams(filters).toString();
    const { data } = await api.get(`/produtos/buscar?${params}`);
    return data;
  },

  async getProductById(id) {
    const { data } = await api.get(`/produtos/${id}`);
    return data;
  }
};
```

**Styling Guidelines:**

- Use Tailwind CSS for all styling
- Mobile-first responsive design
- Avoid inline styles
- Use semantic HTML elements
- Prefer utility classes over custom CSS

---

## Testing

### Backend Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=PedidoServiceTest

# Run with coverage report
mvn verify
mvn jacoco:report
# View report: target/site/jacoco/index.html
```

**Test Naming:**

- Test classes: `{ClassName}Test`
- Test methods: `deve{Action}{Expected}` (e.g., `deveCriarPedidoComSucesso`)
- Use `@SpringBootTest` for integration tests
- Mock external dependencies with `@MockBean`

**Example:**

```java
@SpringBootTest
class PedidoServiceTest {
    
    @Autowired
    private PedidoService pedidoService;
    
    @MockBean
    private DomainEventPublisher eventPublisher;
    
    @Test
    void deveCriarPedidoAoFinalizarCheckout() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest();
        request.setEnderecoEntrega("Rua Teste, 123");
        
        // Act
        PedidoDTO pedido = pedidoService.finalizarCheckout("user@test.com", request);
        
        // Assert
        assertThat(pedido).isNotNull();
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CRIADO);
        verify(eventPublisher, times(1)).publishAll(anyList());
    }
}
```


### Frontend Tests

- **Framework:** Vitest (recommended, not yet configured)
- **Component Testing:** React Testing Library
- **Test Naming:** `Component.test.jsx` or `.spec.jsx`

---

## Database Migrations

**Flyway Migrations:**

- Location: `src/main/resources/db/migration/`
- Naming: `V{version}__{description}.sql`
    - Example: `V5__add_rastreamento_fields_to_pedidos.sql`
- **NEVER** modify an already applied migration
- Create new migration to fix/change previous ones

**Migration Commands:**

```bash
# Migrations run automatically on app start
mvn spring-boot:run

# Clean database (CAREFUL!)
mvn flyway:clean

# Migrate manually
mvn flyway:migrate

# Show migration status
mvn flyway:info
```

**Creating Migration:**

```sql
-- V5__add_rastreamento_fields_to_pedidos.sql

ALTER TABLE pedidos
ADD COLUMN codigo_rastreio VARCHAR(100);

ALTER TABLE pedidos
ADD COLUMN transportadora VARCHAR(100);

CREATE INDEX idx_pedidos_codigo_rastreio ON pedidos(codigo_rastreio);
```

**PostgreSQL-specific:** Use `DO $ BEGIN ... END $` blocks for conditional DDL
**H2-specific:** Use `IF NOT EXISTS` clauses

---

## Domain Events

**Pattern:**

1. Entity emits event when state changes
2. Event is registered internally (`registrarEvento()`)
3. After saving, publish events (`eventPublisher.publishAll()`)
4. Clear events from aggregate (`limparEventos()`)

**Example:**

```java
// In Use Case
Pedido pedido = new Pedido(...);
pedido = pedidoRepository.save(pedido);

// Register creation event (after save, so ID exists)
pedido.registrarCriacao();

// Publish all events
eventPublisher.publishAll(pedido.getDomainEvents());
pedido.limparEventos();
```

**Event Listener:**

```java
@Component
@RequiredArgsConstructor
public class PedidoEventListener {
    
    private final EmailService emailService;
    
    @Async
    @EventListener
    public void handlePedidoCriado(PedidoCriadoEvent event) {
        emailService.enviarEmailPedidoCriado(
            event.getEmailUsuario(),
            event.getNomeUsuario(),
            event.getPedidoId(),
            event.getValorTotal()
        );
    }
}
```


---

## Security

- **Authentication:** JWT tokens stored in localStorage
- **Authorization:** Role-based with `@PreAuthorize`
    - `@PreAuthorize("isAuthenticated()")` - any logged user
    - `@PreAuthorize("hasRole('ADMIN')")` - admin only
- **Validation:** Always use `@Valid` on request bodies
- **SQL Injection:** Prevented by JPA/Hibernate
- **CORS:** Configured in `CorsConfig.java`

---

## API Documentation

**Swagger UI:** http://localhost:8080/api/swagger-ui.html

**Key Endpoints:**

- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `GET /api/produtos/buscar` - Search products
- `POST /api/carrinho/adicionar` - Add to cart
- `POST /api/checkout/finalizar` - Finalize checkout
- `GET /api/meus-pedidos` - List user orders

---

## Common Workflows

### Adding a New Feature

1. **Create Use Case:**

```bash
# Create file: application/usecase/{domain}/{Action}{Entity}UseCase.java
```

2. **Implement Business Logic in Entity:**

```java
// domain/entity/Pedido.java
public void novoMetodo() {
    // validation
    // state change
    // emit event
}
```

3. **Create DTO:**

```bash
# Create file: application/dto/{domain}/{Entity}DTO.java
```

4. **Update Mapper:**

```java
// application/mapper/PedidoMapper.java
```

5. **Create Controller Endpoint:**

```java
// presentation/controller/PedidoController.java
```

6. **Write Tests:**

```bash
# Create file: src/test/.../UseCaseTest.java
```

7. **Test manually via Swagger**

### Frontend Development (Hot Reload Active)

```bash
# Start dev server ONCE
cd minhavenda-frontend
npm run dev

# Keep terminal open - Vite auto-reloads on changes
# NO NEED TO RESTART for code changes!
```

**When to Restart:**

- Added new dependency (`npm install`)
- Changed `vite.config.js`
- Changed environment variables
- Server crashed

**Do NOT:**

- Restart for every code change
- Run `npm run build` during development
- Start multiple dev servers

---

## Common Pitfalls

### Pedido with valorTotal = 0.00

**Symptom:** Order created with `valorTotal = 0.00` but `quantidadeItens > 0`.

**Root Cause:**

- `ItemPedido` has a `subtotal` field calculated only on `@PrePersist`/`@PreUpdate`
- When `Pedido.calcularValorTotal()` calls `item.getSubtotal()` before persisting, it returns `null`
- The stream filters out `null` values, resulting in zero total

**Solution:**
Override `getSubtotal()` in `ItemPedido` to calculate dynamically if not yet persisted:

```java
@Entity
@Table(name = "itens_pedido")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemPedido {
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @PrePersist
    @PreUpdate
    protected void calcularSubtotal() {
        this.subtotal = calcularSubtotalAtual();
    }
    
    // Override Lombok getter
    public BigDecimal getSubtotal() {
        if (this.subtotal != null) {
            return this.subtotal;
        }
        return calcularSubtotalAtual();
    }
    
    private BigDecimal calcularSubtotalAtual() {
        if (quantidade != null && precoUnitario != null) {
            return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        }
        return BigDecimal.ZERO;
    }
}
```

**Use Case Pattern (Correct Order):**

```java
// 1. Create pedido with zero values (will be calculated)
Pedido pedido = new Pedido(
    usuario,
    BigDecimal.ZERO,
    valorFrete,
    valorDesconto,
    BigDecimal.ZERO,
    enderecoEntrega,
    observacoes
);

// 2. Add items BEFORE saving
for (ItemCarrinho item : carrinho.getItens()) {
    pedido.adicionarItem(item.getProduto(), item.getQuantidade(), item.getPrecoUnitario());
}

// 3. Save (now with correct values)
Pedido pedidoSalvo = pedidoRepository.save(pedido);
```


### Missing quantidade_itens Field

**Symptom:** SQL error: `null value in column "quantidade_itens" violates not-null constraint`

**Solution:** Add the field to `Pedido` entity and update it when items change:

```java
@Column(name = "quantidade_itens", nullable = false)
private Integer quantidadeItens = 0;

public void adicionarItem(ItemPedido item) {
    this.itens.add(item);
    item.setPedido(this);
    this.calcularValorTotal();
    this.quantidadeItens = this.getQuantidadeTotal();  // Update count
    this.dataAtualizacao = Instant.now();
}
```


### DevTools Not Hot Reloading

**Check:**

1. DevTools dependency in `pom.xml`
2. IDE auto-compile enabled (IntelliJ: `Build project automatically`)
3. Look for `LiveReload server is running on port 35729` in console

**If not working:** Use `Ctrl+F9` (IntelliJ) or `Ctrl+B` (Eclipse) to force rebuild.

---

## Troubleshooting

### Backend Issues

**Port 8080 already in use:**

```bash
# Find process
lsof -i :8080
# Kill process
kill -9 <PID>
```

**Database migration failed:**

```bash
# Check migration status
mvn flyway:info

# If needed, repair
mvn flyway:repair
```

**Tests failing:**

```bash
# Run with verbose output
mvn test -X

# Skip tests temporarily
mvn spring-boot:run -DskipTests
```


### Frontend Issues

**Port 5173 in use:**

```bash
# Vite will auto-increment (5174, 5175, etc.)
# Or kill existing process
pkill -f vite
```

**Module not found:**

```bash
# Clean install
rm -rf node_modules package-lock.json
npm install
```

**Tailwind classes not working:**

```bash
# Check tailwind.config.js content paths
# Restart dev server
```


---

## Git Workflow

**Commit Message Format:**

```
type(scope): subject

[optional body]

[optional footer]
```

**Types:**

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance

**Examples:**

```
feat(pedido): adicionar campo codigo_rastreio

- Adicionar migration V5
- Atualizar entidade Pedido
- Criar evento PedidoEnviado

fix(checkout): corrigir validação de estoque

- Validar estoque antes de criar pedido
- Adicionar mensagem de erro clara
```

**Before Committing:**

```bash
# Run tests
mvn test

# Run linter (if applicable)
mvn checkstyle:check
```


---

## Important Notes

### Date/Time Handling

- **Entities:** Use `Instant` (UTC timezone)
- **DTOs:** Use `LocalDateTime` (local timezone)
- **Conversion:** Done in Mapper using `ZoneId.of("America/Sao_Paulo")`

**Example:**

```java
// PedidoMapper.java
private LocalDateTime toLocalDateTime(Instant instant) {
    if (instant == null) return null;
    return LocalDateTime.ofInstant(instant, ZoneId.of("America/Sao_Paulo"));
}
```


### Email Configuration

**Development:** Use Mailtrap or Gmail with app password
**Production:** Use SendGrid or Amazon SES

**Config:**

```yaml
# application.yml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
```


### Environment Variables

**Never commit:**

- Database passwords
- API keys
- Email passwords
- JWT secrets

**Use:**

- `application-local.yml` (gitignored)
- Environment variables
- `.env` file (gitignored)

---

## Key Dependencies

**Backend:**

- Spring Boot 3.2.1
- Java 17
- Spring Data JPA
- Spring Security
- Flyway
- Lombok
- SpringDoc OpenAPI (Swagger)
- PostgreSQL / H2

**Frontend:**

- React 19
- Vite 5
- Tailwind CSS
- Axios
- React Router v7
- React Hook Form

---

## Additional Resources

- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [React Docs](https://react.dev/)
- [Tailwind CSS](https://tailwindcss.com/docs)
- [Flyway Docs](https://flywaydb.org/documentation/)
- [DDD Reference](https://www.domainlanguage.com/ddd/)

---

**Last Updated:** 2026-02-02

For questions or issues, consult this file first. It's designed to help AI coding agents understand and work effectively with this codebase.

```