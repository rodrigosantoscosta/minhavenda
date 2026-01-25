# ==========================================
# DOCKERFILE - BACKEND SPRING BOOT (RENDER)
# ==========================================
# Versão simplificada para Render
# Usa Maven instalado na imagem (não wrapper)
# ==========================================

# ==========================================
# STAGE 1: BUILD
# ==========================================
FROM maven:3.9-eclipse-temurin-17-alpine AS build

# Metadata
LABEL maintainer="MinhaVenda Team"
LABEL description="MinhaVenda Backend"

# Diretório de trabalho
WORKDIR /app

# Copiar pom.xml primeiro (cache de dependências)
COPY pom.xml .

# Download de dependências (cache layer)
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Build da aplicação (pula testes para build mais rápido)
RUN mvn clean package -DskipTests -B

# ==========================================
# STAGE 2: RUNTIME
# ==========================================
FROM eclipse-temurin:17-jre-alpine

# Metadata
LABEL maintainer="MinhaVenda Team"
LABEL version="1.0.0"

# Criar usuário não-root (segurança)
RUN addgroup -S spring && adduser -S spring -G spring

# Diretório de trabalho
WORKDIR /app

# Copiar JAR do stage de build
COPY --from=build /app/target/*.jar app.jar

# Mudar ownership para usuário spring
RUN chown -R spring:spring /app

# Trocar para usuário não-root
USER spring:spring

# Expor porta (Render usa PORT env var)
EXPOSE ${PORT:-8080}

# Variáveis de ambiente (podem ser sobrescritas)
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Comando de inicialização
# Render define $PORT automaticamente
CMD java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar