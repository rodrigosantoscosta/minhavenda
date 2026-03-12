package br.com.minhavenda.minhavenda.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro de rate limiting para endpoints de autenticacao.
 *
 * Protege contra ataques de brute-force em:
 *   POST /auth/login
 *   POST /auth/register
 *
 * Estrategia: token bucket por IP de origem.
 *   - Limite: 10 requisicoes por minuto por IP
 *   - Resposta ao exceder: 429 Too Many Requests
 *   - Header Retry-After incluido na resposta
 *
 * Implementacao in-memory (sem Redis) — adequado para instancia unica.
 * Para multiplas instancias, migrar para Bucket4j + Redis/Hazelcast.
 */
@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    /** Maximo de requisicoes por janela de tempo */
    private static final int MAX_REQUESTS_PER_WINDOW = 10;

    /** Janela de tempo para reposicao dos tokens */
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

    /** Buckets por IP — ConcurrentHashMap e thread-safe */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Aplica rate limiting APENAS em /auth/login e /auth/register
        return !(path.endsWith("/auth/login") || path.endsWith("/auth/register"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String clientIp = resolveClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(clientIp, this::newBucket);

        if (bucket.tryConsume(1)) {
            // Dentro do limite — continua o request normalmente
            filterChain.doFilter(request, response);
        } else {
            // Limite excedido — retorna 429
            log.warn("[RateLimit] Limite excedido para IP: {} em {}", clientIp, request.getServletPath());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(WINDOW_DURATION.getSeconds()));
            response.getWriter().write("""
                    {"erro": "Muitas tentativas. Tente novamente em %d segundos.", "status": 429}
                    """.formatted(WINDOW_DURATION.getSeconds()));
        }
    }

    /**
     * Cria um novo Bucket com capacidade de MAX_REQUESTS_PER_WINDOW tokens
     * repostos completamente a cada WINDOW_DURATION.
     */
    private Bucket newBucket(String ip) {
        Bandwidth limit = Bandwidth.classic(
                MAX_REQUESTS_PER_WINDOW,
                Refill.greedy(MAX_REQUESTS_PER_WINDOW, WINDOW_DURATION)
        );
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Resolve o IP real do cliente, considerando proxies/load balancers.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For pode conter multiplos IPs — pega o primeiro (cliente original)
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
