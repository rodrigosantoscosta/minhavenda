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

### 🚀 Optimized Development Process

1. **Start Backend**: `mvn spring-boot:run` (runs on http://localhost:8080)
2. **Start Frontend ONCE**: `cd minhavenda-frontend && npm run dev` (runs on http://localhost:5173)
3. **Access Swagger**: http://localhost:8080/api/swagger-ui.html
4. **Database**: H2 console available during development
5. **HOT RELOAD**: Vite automatically updates browser on file changes - NO RESTART NEEDED

### 📋 Task Execution Protocol

#### **Phase 1: Analysis (Read-Only)**
```bash
# Static code analysis - NO SERVER RESTART
grep -r "pattern" src/ --include="*.jsx,*.js"
read src/components/Component.jsx
glob "src/**/*.jsx"
```

#### **Phase 2: Strategy Planning**
1. Identify root cause of problem
2. Plan complete solution approach
3. Determine which files need changes
4. Group related changes together (batch)

#### **Phase 3: Implementation (Batch Changes)**
```bash
# Make ALL related changes FIRST, then test once
# Leverage Vite's Hot Module Replacement (HMR)
# Example: Fix component + fix props + update styles = ONE TEST
```

#### **Phase 4: Validation (Progressive)**
```bash
# Static validation (preferred)
npm run lint

# Build test (only after batch complete)
npm run build

# Manual browser inspection (only for UI issues)
# Use dev tools without server restart
```

### 🎯 Anti-Patterns to Avoid

❌ **NEVER**: Restart dev server for component changes  
❌ **NEVER**: Run `npm run dev` for each small fix  
❌ **NEVER**: Test every change individually with full server restart  
❌ **NEVER**: Open multiple terminal sessions
❌ **NEVER**: Run npm run dev multiple times in same session

✅ **ALWAYS**: Use Vite's hot reload - NO RESTART NEEDED for React components
✅ **ALWAYS**: Batch related changes together to minimize server restarts
✅ **ALWAYS**: Prefer static analysis (grep, read, glob) over server restarts
✅ **ALWAYS**: Single dev server session per development session

✅ **ALWAYS**: Use Vite's hot reload for React components  
✅ **ALWAYS**: Batch related changes together  
✅ **ALWAYS**: Static analysis before implementation  
✅ **ALWAYS**: Single dev server session per development session



#### **ReferenceError in useEffect**
- **Problem**: Cannot access 'validation' before initialization
- **Root Cause**: useMemo declared after useEffect that uses it
- **Solution**: Proper declaration order (memo/useCallback before useEffect)
- **Key Learning**: JavaScript hoisting rules apply to React hooks

### 🎯 Debugging Framework

#### **1. Problem Identification**
```javascript
// ✅ Pattern recognition for common issues
const problemPatterns = {
  'React does not recognize prop': 'Missing component interface',
  'Cannot access before initialization': 'Hook declaration order',
  'Component updating multiple times': 'useEffect dependency loops',
  'Element positioning conflicts': 'Layout architecture issues'
}
```

#### **2. Root Cause Analysis**
```javascript
// ✅ Always go beyond surface symptoms
const rootCauseAnalysis = {
  symptom: 'Component flickering',
  immediate_cause: 'Multiple re-renders',
  root_cause: 'useEffect dependency loops',
  contributing_factors: ['Multiple state updates', 'Missing debounce']
}
```

#### **3. Solution Strategy**
```javascript
// ✅ Address multiple layers simultaneously
const solutionStrategy = {
  immediate_fix: 'Add debounce to prevent loops',
  structural_improvement: 'Consolidate state updates',
  performance_optimization: 'Memoize expensive operations',
  prevention: 'Establish hook dependency patterns'
}
```

### 📈 Best Practices Established

#### **Component Development**
1. **Interface First**: Define props interface before implementation
2. **Dependency Management**: Stable dependencies in useCallback/useEffect
3. **State Batching**: Atomic updates to prevent cascading renders
4. **Performance First**: Debounce + memoization for optimization
5. **Layout Coesion**: Integrate actions within component boundaries

#### **Debugging Process**
1. **Static Analysis**: grep, read, glob for pattern identification
2. **Problem Isolation**: Identify root cause vs symptoms
3. **Strategic Planning**: Plan complete solution before implementation
4. **Batch Implementation**: Group related changes together
5. **Progressive Testing**: Validate incrementally without restarts

#### **Code Quality**
1. **Prevention over Correction**: Establish patterns to prevent common issues
2. **Documentation**: Update AGENTS.md with lessons learned
3. **Architecture**: Prefer structural solutions over CSS fixes
4. **Performance**: Consider impact of every change on render cycles
5. **Maintainability**: Create reusable, extensible components

### 🔄 Performance-First Approach

```javascript
// ✅ GOOD: Atomic state updates (prevents render loops)
const handleInputChange = useCallback((field, value) => {
  setFormData(prev => {
    const newData = { ...prev, [field]: value }
    // Handle related field updates in single operation
    if (field === 'cep') {
      newData.rua = ''; newData.bairro = ''; newData.cidade = ''
    }
    return newData
  })
}, [])

// ✅ GOOD: Debounced validation (prevents excessive renders)
useEffect(() => {
  const timeoutId = setTimeout(() => {
    const validation = validateForm(formData)
    setErrors(validation.errors)
  }, 300)
  return () => clearTimeout(timeoutId)
}, [formData])

// ❌ BAD: Multiple state updates per interaction
setFormData(prev => ({ ...prev, [field]: value }))
setIsTouched(true)
setErrors(prev => ({ ...prev, [field]: '' }))
```

### 📊 Error Analysis Framework

#### **1. Static Analysis (Preferred)**
- **Syntax/Reference Errors**: `grep`, `read` pattern matching
- **Import/Export Issues**: Dependency analysis
- **Props Interface**: Component contract verification
- **Hook Dependencies**: useEffect dependency array validation

#### **2. Build Analysis (Secondary)**
- **Compilation Errors**: `npm run build` after batch changes
- **TypeScript Errors**: If applicable
- **Bundle Analysis**: Size/performance metrics

#### **3. Runtime Analysis (Last Resort)**
- **Console Errors**: Browser dev tools only
- **Behavioral Issues**: Manual testing only
- **Network Issues**: Dev tools network tab

### 🛠️ Debugging Best Practices

#### **Component Performance Issues**
```javascript
// ✅ Use React DevTools Profiler
// ✅ Memoize expensive calculations with useMemo
// ✅ Use useCallback for event handlers
// ✅ Avoid dependencies that cause re-renders
```

#### **State Management Issues**
```javascript
// ✅ Atomic operations to prevent race conditions
// ✅ Debounce validation and API calls
// ✅ Use refs for values that don't trigger re-renders
// ❌ Avoid useEffect dependency loops
```

#### **Layout/Styling Issues**
```javascript
// ✅ Check Tailwind classes in browser dev tools
// ✅ Use responsive prefixes consistently
// ✅ Test in multiple viewport sizes
// ✅ Verify z-index stacking context
```

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

## 📋 Session Management & Development Protocol

### 🔄 Single Development Session Strategy

#### **Session Initialization**
```bash
# Start ONCE per development session
npm run dev  # Runs on :5173, :5174, :5175, etc.
# Keep terminal open, leverage hot reload
```

#### **Process Management**
```bash
# Check existing Vite processes
ps aux | grep -i vite
# Kill orphaned processes if needed
taskkill /PID <number> (Windows)
kill -9 <PID> (Linux/Mac)
```

#### **Port Management**
```bash
# Check available ports
netstat -an | grep :5173
# Or use lsof for specific port
lsof -i :5173
```

### 📋 Development Protocol Summary

#### **When Starting New Session**
1. ✅ Check for existing Vite processes
2. ✅ Kill orphaned processes if found
3. ✅ Start single `npm run dev` instance
4. ✅ Use hot reload for React changes
5. ✅ Only restart if server crashes or hangs

#### **During Development**
1. ✅ Static analysis first (grep, read, glob)
2. ✅ Batch related changes together
3. ✅ Let Vite handle hot reload automatically
4. ✅ Manual browser testing for UI issues
5. ✅ Build testing only after batch completion

#### **When Encountering Issues**
1. ✅ Analyze error patterns with static tools
2. ✅ Check console without server restart
3. ✅ Use React DevTools for component inspection
4. ✅ Network tab for API issues
5. ✅ Only restart as last resort


### 🛠️ Tools for Effective Session Management

#### **Process Monitoring**
```bash
# List all Node.js processes
ps aux | grep node
# Monitor port usage
netstat -tulpn | grep :517
# Find and kill processes
pkill -f vite
```

#### **Browser Development**
- React DevTools for component inspection
- Console for runtime errors
- Network tab for API debugging
- Performance tab for optimization
- Elements panel for layout issues

#### **IDE/Editor Integration**
- Use built-in terminals instead of external
- Configure hot reload preferences
- Set up error highlighting
- Enable code completion and linting

## Advanced Patterns & Best Practices

### 🎯 Component Anti-Patterns to Avoid

#### **❌ Performance Anti-Patterns**
```javascript
// ❌ Multiple state updates per interaction
setFormData(prev => ({ ...prev, [field]: value }))
setIsTouched(true)
setErrors(prev => ({ ...prev, [field]: '' }))

// ✅ Atomic state update
setFormData(prev => {
  const newData = { ...prev, [field]: value }
  if (field === 'cep') {
    newData.rua = ''; newData.bairro = ''; newData.cidade = ''
  }
  return newData
})
setIsTouched(true)
```

#### **❌ useEffect Dependency Loops**
```javascript
// ❌ Callback recreation causing loops
useEffect(() => {
  // validation logic
}, [formData, onAddressChange]) // onAddressChange changes frequently

// ✅ Stable dependencies
const memoizedOnAddressChange = useCallback((data) => {
  onAddressChange?.(data)
}, [onAddressChange])

useEffect(() => {
  // validation logic with debounce
}, [formData, memoizedOnAddressChange])
```

#### **❌ Props Interface Issues**
```javascript
// ❌ Unsupported props
<Input icon={<FiMail />} // 'icon' not supported
       rightElement={<Button />} // 'rightElement' not supported

// ✅ Correct props
<Input leftIcon={<FiMail />} 
       rightElement={<Button />} // implemented in component
```

### 🔧 Common Solutions for React Performance Issues

#### **1. Debounce for Validation**
```javascript
useEffect(() => {
  const timeoutId = setTimeout(() => {
    if (isTouched) {
      const validation = validarEndereco(formData)
      setErrors(validation.errors)
      onAddressChange?.({
        address: formData,
        isValid: validation.isValid,
        errors: validation.errors
      })
    }
  }, 300) // Debounce for 300ms

  return () => clearTimeout(timeoutId)
}, [formData, isTouched, memoizedOnAddressChange])
```

#### **2. Memoization for Expensive Operations**
```javascript
// ✅ Memoize validation result
const validation = useMemo(() => validarEndereco(formData), [formData])

// ✅ Memoize callback functions
const handleInputChange = useCallback((field, value) => {
  // implementation
}, [errors]) // Only depend on what's actually used
```

#### **3. State Update Batching**
```javascript
// ✅ Use single update function
const handleInputChange = useCallback((field, value) => {
  setFormData(prev => {
    const newData = { ...prev, [field]: value }
    
    // Handle all related logic in one go
    if (field === 'cep') {
      const cepNumbers = value.replace(/\D/g, '')
      if (cepNumbers.length < 8) {
        newData.rua = ''; newData.bairro = ''; 
        newData.cidade = ''; newData.estado = ''
      }
    }
    
    return newData
  })
  
  // Batch related state updates
  setIsTouched(true)
  setCepNotFound(false)
}, [])
```

### 🐛 Debugging Troubleshooting Guide

#### **ReferenceError: Cannot access 'X' before initialization**
```
Cause: Variable declared with const/let used before declaration
Solution: Move useMemo/useCallback declarations before useEffect
Example: Move validation useMemo before useEffect that uses it
```

#### **React does not recognize prop 'X' on a DOM element**
```
Cause: Component passing unknown props to HTML elements via {...props}
Solution: Destructure and filter only valid props, or implement missing prop
Example: Add rightElement to Input component props interface
```

#### **Component updating multiple times/flickering**
```
Cause: useEffect dependencies causing re-render loops
Solution: 
1. Add debounce to validation effects
2. Use stable callback references (useCallback)
3. Batch state updates
4. Memoize expensive calculations
```