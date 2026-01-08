package br.com.minhavenda.minhavenda.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Serviço para manipulação de tokens JWT.
 *
 * Responsabilidades:
 * - Gerar tokens JWT
 * - Validar tokens JWT
 * - Extrair informações do token (email, expiração, etc)
 *
 * JWT (JSON Web Token):
 * - Header: tipo do token e algoritmo
 * - Payload: dados do usuário (claims)
 * - Signature: assinatura para validar integridade
 */
@Service
public class JwtService {

    /**
     * Chave secreta para assinar o token.
     * DEVE estar em variável de ambiente (.env) em produção!
     *
     * Gerar chave segura:
     * openssl rand -base64 64
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Tempo de expiração do token em milissegundos.
     */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Extrai o email (subject) do token JWT.
     *
     * @param token JWT token
     * @return email do usuário
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrai uma claim específica do token.
     *
     * @param token JWT token
     * @param claimsResolver função para extrair a claim desejada
     * @return valor da claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Gera token JWT para um usuário.
     *
     * @param userDetails dados do usuário
     * @return token JWT
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Gera token JWT com claims customizadas.
     *
     * Claims padrão:
     * - sub (subject): email do usuário
     * - iat (issued at): data de criação
     * - exp (expiration): data de expiração
     *
     * @param extraClaims claims adicionais (roles, permissions, etc)
     * @param userDetails dados do usuário
     * @return token JWT
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
                .builder()
                // Claims customizadas (ex: roles, permissions)
                .setClaims(extraClaims)

                // Subject (identificador único do usuário - email)
                .setSubject(userDetails.getUsername())

                // Data de criação do token
                .setIssuedAt(new Date(System.currentTimeMillis()))

                // Data de expiração (agora + jwtExpiration)
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))

                // Assina o token com a chave secreta
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)

                // Gera a string do token
                .compact();
    }

    /**
     * Valida se o token é válido para o usuário.
     *
     * Validações:
     * - Email no token corresponde ao email do usuário
     * - Token não está expirado
     *
     * @param token JWT token
     * @param userDetails dados do usuário
     * @return true se válido, false caso contrário
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Verifica se o token está expirado.
     *
     * @param token JWT token
     * @return true se expirado, false caso contrário
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrai a data de expiração do token.
     *
     * @param token JWT token
     * @return data de expiração
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrai todas as claims do token.
     *
     * @param token JWT token
     * @return todas as claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                // Define a chave para validar a assinatura
                .setSigningKey(getSignInKey())
                .build()
                // Faz o parse do token
                .parseClaimsJws(token)
                // Extrai o body (claims)
                .getBody();
    }

    /**
     * Converte a chave secreta (String) em Key (objeto).
     *
     * @return chave de assinatura
     */
    private Key getSignInKey() {
        // Decodifica a chave de Base64 para bytes
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);

        // Cria uma chave HMAC
        return Keys.hmacShaKeyFor(keyBytes);
    }
}