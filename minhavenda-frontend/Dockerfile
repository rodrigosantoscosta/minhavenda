# Dockerfile - FRONTEND REACT
# Multi-stage build para otimizar tamanho da imagem

# Stage 1: Build
FROM node:20-alpine AS build

WORKDIR /app

# Copiar package files e instalar dependências (cachear camada)
COPY package*.json ./
RUN npm ci

# Copiar código fonte
COPY . .

# Build da aplicação
RUN npm run build

# Stage 2: Runtime com Nginx
FROM nginx:alpine

# Copiar arquivos buildados
COPY --from=build /app/dist /usr/share/nginx/html

# Copiar configuração customizada do Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expor porta
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:80 || exit 1

# Nginx roda em foreground por padrão
CMD ["nginx", "-g", "daemon off;"]
