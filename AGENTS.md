# AGENTS.md - Guide for Agentic Coding

This file provides guidance for AI agents working with the MinhaVenda e-commerce codebase.

## Overview

MinhaVenda is a full-stack e-commerce application with a Spring Boot backend following Clean Architecture/DDD principles and a React frontend with Vite.

## Build/Test Commands

### Backend (Java/Spring Boot - Root Directory)
```bash
# Run the application with H2 (development)
mvn spring-boot:run

# Build the project
mvn clean package

# Run tests
mvn test

# Run single test class
mvn test -Dtest=MinhavendaApplicationTests

# Run tests with coverage
mvn verify
mvn jacoco:report

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Generate JAR
mvn clean package -DskipTests
java -jar target/minhavenda-1.0.0.jar
```

### Frontend (React - minhavenda-frontend/)
```bash
cd minhavenda-frontend

# Install dependencies
npm install

# Run development server
npm run dev

# Build for production
npm run build

# Run linter
npm run lint

# Preview production build
npm run preview
```

### Docker Services
```bash
# Start PostgreSQL and RabbitMQ
docker-compose up -d

# Stop services
docker-compose down
```

## Project Structure

### Backend Architecture (Clean Architecture/DDD)

```
src/main/java/br/com/minhavenda/minhavenda/
├── 📱 presentation/           # REST Controllers
├── 🎯 application/           # Use Cases, DTOs, Mappers
├── 🏛️ domain/               # Entities, Value Objects, Domain Rules
├── 🔧 infrastructure/        # Repositories, External Services
└── ⚙️ config/               # Security, Database Config
```

**Key Patterns:**
- Controllers handle HTTP and delegate to Use Cases
- Use Cases orchestrate business logic
- Entities contain business rules and validation
- Repositories abstract data access
- Mappers handle Entity ↔ DTO conversion

### Frontend Structure

```
minhavenda-frontend/src/
├── components/              # Reusable UI components
├── pages/                  # Route-level components
├── services/               # API service layer
├── contexts/               # React contexts (Auth, Cart)
├── utils/                  # Utility functions
└── App.jsx                 # Main routing setup
```

## Code Style Guidelines

### Backend (Java)

**Naming Conventions:**
- Classes: PascalCase (ProdutoController, ListarProdutosUseCase)
- Methods: camelCase (buscarPorId, executar)
- Variables: camelCase (produtoId, nomeUsuario)
- Constants: UPPER_SNAKE_CASE (DEFAULT_PAGE_SIZE)
- Packages: lowercase (br.com.minhavenda.minhavenda)

**Code Organization:**
- Use Lombok annotations (@Getter, @NoArgsConstructor, @Builder)
- Entity constructors: protected no-args, private all-args
- Business logic in domain entities, not in services
- Use Cases follow pattern: {Action}{Entity}UseCase
- DTOs end with suffix: DTO, Request, Response

**Validation & Error Handling:**
- Use Jakarta validation annotations (@NotBlank, @Min, @Valid)
- Business rules throw IllegalArgumentException with clear messages
- Controllers return ResponseEntity with appropriate HTTP status
- Use @PreAuthorize for security checks

**Database Patterns:**
- UUID primary keys with @GeneratedValue(strategy = GenerationType.UUID)
- @CreationTimestamp for audit fields
- JPA relationships: LAZY fetching, proper cascade/cascade removal
- Use value objects for complex fields (Money, Email)

**Examples:**
```java
// Entity pattern
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Produto {
    // Business methods in entities
    public void atualizarPreco(Money novoPreco) {
        if (novoPreco == null || novoPreco.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço deve ser maior que zero");
        }
        this.preco = novoPreco;
    }
}

// Use Case pattern
@Service
@RequiredArgsConstructor
public class BuscarProdutoPorIdUseCase {
    private final ProdutoRepository produtoRepository;
    
    public Optional<ProdutoDTO> executar(UUID id) {
        return produtoRepository.findById(id)
                .map(produtoMapper::toDTO);
    }
}

// Controller pattern
@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Produtos", description = "Endpoints para gerenciamento de produtos")
public class ProdutoController {
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoDTO> buscarPorId(@PathVariable UUID id) {
        return buscarProdutoPorIdUseCase.executar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
```

### Frontend (React/JavaScript)

**Component Organization:**
- Functional components with hooks
- Custom hooks for complex logic
- Props destructuring
- TypeScript-style prop validation with PropTypes (if needed)

**Styling:**
- Tailwind CSS for all styling
- Consistent color palette: primary colors defined in tailwind.config.js
- Responsive design with mobile-first approach
- Use semantic HTML elements

**State Management:**
- React Context for global state (AuthContext, CartContext)
- Local state with useState/useReducer
- Server state via API service layer
- No external state management library

**API Integration:**
- Axios instance with interceptors in services/api.js
- Service layer for each domain (authService.js, productService.js)
- Consistent error handling with try/catch
- Token management in localStorage

**Routing:**
- React Router v7 with nested routes
- Protected routes with ProtectedRoute component
- Route-based code splitting (lazy loading for large pages)

**Examples:**
```jsx
// Component pattern
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
      <h3 className="font-semibold text-lg mb-2">{product.nome}</h3>
      <p className="text-primary-600 font-bold">R$ {product.preco}</p>
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

// Service pattern
export const productService = {
  async getProducts(filters = {}) {
    const params = new URLSearchParams(filters).toString();
    return await get(`/produtos/buscar?${params}`);
  },

  async getProductById(id) {
    return await get(`/produtos/${id}`);
  }
};
```

## Testing

### Backend
- JUnit 5 for unit tests
- Spring Boot Test for integration tests
- Test naming: {ClassName}Test
- Use @SpringBootTest for integration tests
- Mock external dependencies with @MockBean

### Frontend
- No specific test framework configured yet
- When adding tests: consider Vitest or React Testing Library
- Test naming: Component.test.jsx or .spec.jsx

## Development Workflow

1. **Start Backend**: `mvn spring-boot:run` (runs on http://localhost:8080)
2. **Start Frontend**: `cd minhavenda-frontend && npm run dev` (runs on http://localhost:5173)
3. **Access Swagger**: http://localhost:8080/api/swagger-ui.html
4. **Database**: H2 console available during development

## Key Dependencies

### Backend
- Spring Boot 3.2.1, Java 17
- Spring Data JPA, Spring Security, Validation
- PostgreSQL (production), H2 (development)
- Lombok, MapStruct
- SpringDoc OpenAPI (Swagger)

### Frontend
- React 19, Vite
- Tailwind CSS
- Axios for HTTP client
- React Router v7
- React Hook Form

## Security Considerations

- JWT tokens stored in localStorage
- Role-based access control (@PreAuthorize)
- Input validation on both frontend and backend
- SQL injection prevention via JPA/Hibernate
- CORS configuration for API access

## Database Migrations

- Flyway for database versioning
- Migration files in `src/main/resources/db/migration/`
- Naming convention: V{number}__description.sql