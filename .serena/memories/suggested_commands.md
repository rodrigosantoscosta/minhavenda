# Comandos Sugeridos - MinhaVenda (Windows)

## Backend
```bash
mvn spring-boot:run                          # Dev com H2
mvn clean package                            # Build
mvn test                                     # Todos os testes
mvn test -Dtest=NomeClasseTest               # Teste específico
mvn spring-boot:run -Dspring-boot.run.profiles=prod  # Perfil prod
```

## Docker
```bash
docker-compose up -d     # Sobe PostgreSQL + RabbitMQ
docker-compose down      # Para serviços
docker-compose logs -f   # Ver logs
```

## Frontend
```bash
cd minhavenda-frontend
npm install
npm run dev              # Dev server http://localhost:5173
npm run build
npm run lint
```

## Utilitários (Windows)
```cmd
dir /b          # listar arquivos
type            # ler arquivo
findstr         # grep equivalente
```

## URLs
- Backend: http://localhost:8080
- Swagger: http://localhost:8080/api/swagger-ui.html
- H2: http://localhost:8080/h2-console
- RabbitMQ Management: http://localhost:15672
