package br.com.minhavenda.minhavenda.config;

import br.com.minhavenda.minhavenda.infrastructure.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração de Segurança da aplicação.
 *
 * Responsabilidades:
 * - Configurar autenticação JWT
 * - Definir endpoints públicos e protegidos
 * - Configurar CORS
 * - Configurar BCrypt para hash de senhas
 *
 * ENDPOINTS PÚBLICOS (não precisam autenticação):
 * - /api/auth/** - Login e Register
 * - /swagger-ui/** - Documentação Swagger
 * - /v3/api-docs/** - OpenAPI
 * - /h2-console/** - Console H2 (apenas DEV)
 *
 * ENDPOINTS PROTEGIDOS (precisam token JWT):
 * - Todos os outros (produtos, categorias, pedidos, etc)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Permite usar @PreAuthorize, @Secured nos controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Configuração principal de segurança.
     *
     * Define:
     * - Quais endpoints são públicos
     * - Quais endpoints precisam autenticação
     * - Como validar tokens JWT
     * - Política de sessão (stateless)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Desabilita CSRF
                // CSRF não é necessário para APIs REST stateless (usa JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Configuração de autorização
                .authorizeHttpRequests(auth -> auth
                        // ENDPOINTS PÚBLICOS (não precisam autenticação)
                        .requestMatchers(
                                "/auth/**",
                                "/swagger-ui/**",         // Swagger UI
                                "/v3/api-docs/**",        // OpenAPI docs
                                "/swagger-ui.html",       // Swagger HTML
                                "/swagger-resources/**",  // Swagger resources
                                "/webjars/**",            // Swagger webjars
                                "/h2-console/**"          // H2 Console (DEV)
                        ).permitAll()

                        // TODOS os outros endpoints precisam autenticação
                        .anyRequest().authenticated()
                )

                // 3. Configura sessão como STATELESS
                // Não usa sessão no servidor (JWT é stateless)
                // Cada request carrega o token JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Configura provider de autenticação
                // Define como validar usuário e senha
                .authenticationProvider(authenticationProvider())

                // 5. Adiciona filtro JWT ANTES do filtro padrão
                // Intercepta todas requests e valida token JWT
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Provider de autenticação.
     *
     * Responsável por:
     * - Carregar usuário do banco (via UserDetailsService)
     * - Validar senha com BCrypt (via PasswordEncoder)
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        // Define como carregar usuário do banco
        authProvider.setUserDetailsService(userDetailsService);

        // Define como validar senha (BCrypt)
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    /**
     * AuthenticationManager - responsável por autenticar usuários.
     *
     * Usado no login para validar credenciais (email + senha).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * PasswordEncoder - BCrypt para hash de senhas.
     *
     * BCrypt:
     * - Gera hash único para cada senha (mesmo senhas iguais têm hashs diferentes)
     * - Adiciona "salt" automático (previne rainbow table attacks)
     * - Lento propositalmente (dificulta brute force)
     * - Strength 10 (padrão) = bom equilíbrio segurança/performance
     *
     * Exemplo:
     * - Senha: "senha123"
     * - Hash: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}