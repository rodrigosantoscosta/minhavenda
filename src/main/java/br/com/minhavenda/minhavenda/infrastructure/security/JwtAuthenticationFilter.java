package br.com.minhavenda.minhavenda.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticação JWT.
 * 
 * Responsabilidades:
 * - Interceptar TODAS as requests
 * - Extrair token JWT do header Authorization
 * - Validar o token
 * - Autenticar o usuário no Spring Security Context
 * 
 * Fluxo:
 * 1. Request chega
 * 2. Extrai token do header "Authorization: Bearer <token>"
 * 3. Valida token e extrai email
 * 4. Carrega usuário do banco
 * 5. Autentica usuário no contexto
 * 6. Passa request adiante
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Executa a cada request (antes de chegar no controller).
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain cadeia de filtros
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 1. Extrai o header Authorization
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Verifica se o header existe e começa com "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Se não tem token, passa adiante (será negado depois se precisar autenticação)
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extrai o token (remove "Bearer " do início)
        jwt = authHeader.substring(7);
        
        // 4. Extrai o email (subject) do token
        userEmail = jwtService.extractUsername(jwt);

        // 5. Se tem email E usuário ainda não está autenticado no contexto
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // 6. Carrega detalhes do usuário do banco de dados
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 7. Valida se o token é válido para este usuário
            if (jwtService.isTokenValid(jwt, userDetails)) {
                
                // 8. Cria objeto de autenticação
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,           // Principal (usuário)
                        null,                  // Credentials (senha - não precisa)
                        userDetails.getAuthorities()  // Authorities (roles/permissions)
                );
                
                // 9. Adiciona detalhes da request no token de autenticação
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // 10. Autentica o usuário no Spring Security Context
                // A partir daqui, o usuário está autenticado para esta request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // 11. Passa a request adiante na cadeia de filtros
        filterChain.doFilter(request, response);
    }
}
