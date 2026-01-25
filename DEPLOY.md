# DEPLOY.md - MinhaVenda Deployment Guide

This comprehensive guide walks you through deploying the MinhaVenda e-commerce application using free hosting services.

## 🏗️ Architecture Overview

```
Frontend (Vercel) ←→ Backend (Render) ←→ Database (Supabase)
     ↓                    ↓                  ↓
   React Vite        Spring Boot API      PostgreSQL
   (Static)          (Java 17)         (Free Tier)
```

## 📋 Prerequisites

- **Accounts**: GitHub, Vercel, Supabase, Render
- **Tools**: Git, Node.js, Java 17, Maven
- **Project**: MinhaVenda codebase

## 🎯 Deployment Strategy

### Phase 1: Database Setup (Supabase)
### Phase 2: Backend Deployment (Render)
### Phase 3: Frontend Deployment (Vercel)
### Phase 4: Testing & Final Configuration

---

## 🗄️ Phase 1: Database Setup (Supabase)

### 1.1 Create Supabase Project
1. Go to [supabase.com](https://supabase.com)
2. Sign up/login with GitHub
3. Click "New Project"
4. Choose organization
5. Configure project:
   - **Project Name**: `minhavenda-db`
   - **Database Password**: Generate strong password
   - **Region**: Choose nearest to you
   - **Pricing Plan**: Free Tier

### 1.2 Get Database Connection String
1. In Supabase dashboard → Settings → Database
2. Scroll down to **Connection string**
3. Copy the **URI** format connection string
4. Replace `[YOUR-PASSWORD]` with your database password

**Connection String Format**:
```
postgresql://postgres:[YOUR-PASSWORD]@db.[PROJECT-REF].supabase.co:5432/postgres
```

### 1.3 Create Database Schema
1. In Supabase dashboard → SQL Editor
2. Click "New query" and paste the migration files from:
   ```
   src/main/resources/db/migration/V001__create_initial_schema.sql
   src/main/resources/db/migration/V006__create_pedido_tables.sql
   ```
3. Execute each migration file sequentially

### 1.4 Configure Database Settings
1. Settings → Database → Connection pooling
2. Enable connection pooling if available
3. Note the connection parameters for backend configuration

---

## ⚙️ Phase 2: Backend Deployment (Render)

### 2.1 Prepare Backend for Production

#### 2.1.1 Update Production Profile
Edit `src/main/resources/application-prod.properties`:
```properties
# Database Configuration
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Server Configuration
server.port=${PORT:8080}
server.servlet.context-path=/api

# CORS Configuration
cors.allowed-origins=${ALLOWED_ORIGINS}

# JWT Configuration
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Hikari Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

#### 2.1.2 Update Security Config for Production
Edit `src/main/java/br/com/minhavenda/minhavenda/config/SecurityConfig.java`:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        System.getenv("ALLOWED_ORIGINS").split(",")
    ));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

#### 2.1.3 Create Render-Specific Web Service
Create `render.yaml` in project root:
```yaml
services:
  - type: web
    name: minhavenda-api
    env: docker
    plan: free
    dockerfilePath: ./Dockerfile
    autoDeploy: true
    envVars:
      - key: SPRING_DATASOURCE_URL
        sync: false
      - key: SPRING_DATASOURCE_USERNAME
        sync: false
      - key: SPRING_DATASOURCE_PASSWORD
        sync: false
      - key: JWT_SECRET
        generateValue: true
      - key: ALLOWED_ORIGINS
        sync: false
      - key: JAVA_OPTS
        value: "-Xmx512m"
```

#### 2.1.4 Create Dockerfile
Create `Dockerfile` in project root:
```dockerfile
# Use official OpenJDK 17 slim image
FROM openjdk:17-slim

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/minhavenda-1.0.0.jar"]
```

#### 2.1.5 Update Maven Wrapper Permissions
```bash
chmod +x mvnw
```

### 2.2 Deploy to Render

#### 2.2.1 Create Render Account
1. Go to [render.com](https://render.com)
2. Sign up with GitHub
3. Authorize Render to access your repositories

#### 2.2.2 Create Web Service
1. Dashboard → "New" → "Web Service"
2. Connect your GitHub repository
3. Configure service:
   - **Name**: `minhavenda-api`
   - **Environment**: `Docker`
   - **Region**: Choose nearest
   - **Branch**: `main`
   - **Plan**: `Free`
4. Add Environment Variables:
   ```
   SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:[PASSWORD]@db.[PROJECT-REF].supabase.co:5432/postgres
   SPRING_DATASOURCE_USERNAME=postgres
   SPRING_DATASOURCE_PASSWORD=[Supabase password]
   JWT_SECRET=[Generate using: openssl rand -base64 32]
   ALLOWED_ORIGINS=https://yourdomain.vercel.app,http://localhost:5173
   ```
5. Click "Create Web Service"

#### 2.2.3 Wait for Deployment
- Deployment takes 5-10 minutes
- Monitor logs in Render dashboard
- Service will be available at: `https://minhavenda-api.onrender.com`

#### 2.2.4 Test Backend API
```bash
# Test health endpoint
curl https://minhavenda-api.onrender.com/api/health

# Test Swagger
# Open: https://minhavenda-api.onrender.com/api/swagger-ui.html
```

---

## 🎨 Phase 3: Frontend Deployment (Vercel)

### 3.1 Create Separate Frontend Repository

#### 3.1.1 Initialize Git Repository in Frontend
```bash
cd minhavenda-frontend
git init
git add .
git commit -m "Initial frontend commit"
```

#### 3.1.2 Create GitHub Repository
1. Go to GitHub → Create new repository
2. Repository name: `minhavenda-frontend`
3. Description: "MinhaVenda E-commerce Frontend"
4. Make it public
5. Push to GitHub:
   ```bash
   git remote add origin https://github.com/yourusername/minhavenda-frontend.git
   git branch -M main
   git push -u origin main
   ```

### 3.2 Configure Frontend for Production

#### 3.2.1 Update Environment Variables
Create `.env.production`:
```env
VITE_API_URL=https://minhavenda-api.onrender.com/api
VITE_APP_NAME=MinhaVenda
VITE_APP_VERSION=1.0.0
```

#### 3.2.2 Create Vercel Configuration
Create `vercel.json`:
```json
{
  "version": 2,
  "builds": [
    {
      "src": "package.json",
      "use": "@vercel/static-build",
      "config": {
        "distDir": "dist"
      }
    }
  ],
  "routes": [
    {
      "src": "/api/(.*)",
      "dest": "https://minhavenda-api.onrender.com/api/$1"
    },
    {
      "src": "/(.*)",
      "dest": "/index.html"
    }
  ],
  "env": {
    "VITE_API_URL": "@api_url"
  },
  "build": {
    "env": {
      "VITE_API_URL": "@api_url"
    }
  }
}
```

#### 3.2.3 Update Vite Configuration
Update `vite.config.js`:
```javascript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  build: {
    outDir: 'dist',
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          router: ['react-router-dom'],
          ui: ['@headlessui/react']
        }
      }
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

#### 3.2.4 Update Package.json Scripts
Update `package.json`:
```json
{
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview",
    "lint": "eslint .",
    "vercel-build": "npm run build"
  }
}
```

### 3.3 Deploy to Vercel

#### 3.3.1 Create Vercel Account
1. Go to [vercel.com](https://vercel.com)
2. Sign up with GitHub
3. Authorize Vercel to access repositories

#### 3.3.2 Import Project
1. Dashboard → "Add New" → "Project"
2. Select `minhavenda-frontend` repository
3. Configure project:
   - **Framework Preset**: `Vite`
   - **Root Directory**: `./`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
   - **Install Command**: `npm install`

#### 3.3.3 Add Environment Variables
```
VITE_API_URL=https://minhavenda-api.onrender.com/api
VITE_APP_NAME=MinhaVenda
VITE_APP_VERSION=1.0.0
```

#### 3.3.4 Deploy
1. Click "Deploy"
2. Wait for deployment (2-3 minutes)
3. Your app will be available at: `https://minhavenda-frontend.vercel.app`

---

## 🔧 Phase 4: Testing & Final Configuration

### 4.1 Update CORS Configuration

#### 4.1.1 Get Vercel Domain
From Vercel dashboard, copy your deployed URL (e.g., `minhavenda-frontend.vercel.app`)

#### 4.1.2 Update Backend CORS
In Render dashboard:
1. Go to `minhavenda-api` → Environment
2. Update `ALLOWED_ORIGINS`:
   ```
   https://minhavenda-frontend.vercel.app,https://www.minhavenda-frontend.vercel.app
   ```
3. Save and trigger redeploy

### 4.2 Test Full Application

#### 4.2.1 Frontend Tests
1. Open `https://minhavenda-frontend.vercel.app`
2. Test user registration/login
3. Test product browsing
4. Test cart functionality
5. Test order creation

#### 4.2.2 Backend Tests
```bash
# Test API endpoints
curl https://minhavenda-api.onrender.com/api/produtos
curl -X POST https://minhavenda-api.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}'
```

### 4.3 Set Up Custom Domain (Optional)

#### 4.3.1 Configure Vercel Domain
1. Vercel dashboard → Project → Settings → Domains
2. Add custom domain (e.g., `minhavenda.app`)
3. Follow DNS configuration instructions

#### 4.3.2 Update Backend CORS
In Render dashboard, update `ALLOWED_ORIGINS` to include:
```
https://minhavenda.app,https://www.minhavenda.app,https://minhavenda-frontend.vercel.app
```

---

## 🔍 Monitoring & Maintenance

### 5.1 Performance Monitoring

#### 5.1.1 Vercel Analytics
- Dashboard → Analytics tab
- Monitor page views, Core Web Vitals
- Check build success rate

#### 5.1.2 Render Monitoring
- Dashboard → Services → `minhavenda-api`
- Monitor response times, error rates
- Check logs for issues

#### 5.1.3 Supabase Monitoring
- Dashboard → Database → Usage
- Monitor database connections, storage
- Check query performance

### 5.2 Security Best Practices

#### 5.2.1 Regular Updates
- Update dependencies monthly
- Monitor security advisories
- Review CORS settings regularly

#### 5.2.2 Backup Strategy
- Supabase provides automatic backups
- Export database schema quarterly
- Keep configuration files in Git

#### 5.2.3 Access Control
- Use environment variables for secrets
- Limit database permissions
- Monitor API usage patterns

---

## 🚨 Troubleshooting Guide

### Common Issues & Solutions

#### Backend Deployment Issues
**Problem**: Build fails on Render
**Solution**: 
- Check Dockerfile syntax
- Verify Maven wrapper permissions
- Review build logs in Render dashboard

**Problem**: Database connection failed
**Solution**:
- Verify Supabase connection string
- Check Supabase network settings
- Ensure database is active

#### Frontend Deployment Issues
**Problem**: API calls failing
**Solution**:
- Verify `VITE_API_URL` environment variable
- Check CORS configuration on backend
- Test API endpoints directly

**Problem**: Build warnings
**Solution**:
- Fix import paths
- Update dependencies
- Check for missing dependencies

#### CORS Issues
**Problem**: CORS policy errors in browser
**Solution**:
- Update `ALLOWED_ORIGINS` in Render
- Check frontend URL matches exactly
- Verify preflight requests are handled

#### Performance Issues
**Problem**: Slow page loads
**Solution**:
- Optimize images and assets
- Enable Vercel Edge Functions
- Check database query performance

---

## 📋 Deployment Checklist

### Pre-Deployment ✅
- [ ] All tests passing locally
- [ ] Environment variables documented
- [ ] Security review completed
- [ ] Database schema finalized
- [ ] Backup strategy in place

### Deployment Process ✅
- [ ] Supabase database created
- [ ] Schema migrations applied
- [ ] Backend deployed to Render
- [ ] Frontend deployed to Vercel
- [ ] CORS configured correctly

### Post-Deployment ✅
- [ ] All user flows tested
- [ ] API endpoints responding
- [ ] Database operations working
- [ ] Monitoring configured
- [ ] Custom domain set (optional)

---

## 📚 Additional Resources

### Documentation Links
- [Vercel Deployment Guide](https://vercel.com/docs)
- [Render Web Services](https://render.com/docs/web-services)
- [Supabase Database](https://supabase.com/docs/guides/database)
- [Spring Boot Production](https://spring.io/guides/topicals/spring-boot-production)

### Useful Commands
```bash
# Build backend locally
mvn clean package -DskipTests

# Build frontend locally
npm run build

# Test API endpoint
curl -I https://your-api.onrender.com/api/health

# Check deployment logs
vercel logs
render logs
```

### Configuration Templates
All configuration files mentioned in this guide are ready to be copied and modified for your specific use case.

---

## 🎉 Congratulations!

You have successfully deployed the MinhaVenda e-commerce application using modern cloud services. Your application is now live and accessible to users worldwide!

**Next Steps:**
1. Set up analytics and monitoring
2. Configure SSL certificates (automatic on Vercel/Render)
3. Set up CI/CD for automated deployments
4. Plan scaling strategy as traffic grows

**Need Help?**
- Check the troubleshooting section above
- Review platform-specific documentation
- Join the respective platform communities for support

---

*Last Updated: January 2026*