package br.com.minhavenda.minhavenda.presentation.controller;

import br.com.minhavenda.minhavenda.application.dto.auth.AuthenticationResponse;
import br.com.minhavenda.minhavenda.application.dto.auth.LoginRequest;
import br.com.minhavenda.minhavenda.application.dto.auth.RegisterRequest;
import br.com.minhavenda.minhavenda.application.usecase.auth.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de Autenticação.
 *
 * Endpoints PÚBLICOS (não precisam autenticação):
 * - POST /api/auth/register - Cadastro
 * - POST /api/auth/login - Login
 */
@RestController
@RequestMapping("/auth")  //  /api (context-path já adiciona)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticação", description = "Endpoints de cadastro e login")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * Cadastro de novo usuário.
     *
     * POST /api/auth/register
     *
     * Body:
     * {
     *   "nome": "João Silva",
     *   "email": "joao@email.com",
     *   "cpf": "123.456.789-00",
     *   "senha": "senha123",
     *   "telefone": "(11) 98765-4321"
     * }
     *
     * Response (201):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "type": "Bearer",
     *   "email": "joao@email.com",
     *   "nome": "João Silva"
     * }
     *
     * Validações:
     * - Email único
     * - CPF único e válido
     * - Senha mínimo 6 caracteres
     *
     * @param request dados do novo usuário
     * @return response com token JWT
     */
    @PostMapping("/register")
    @Operation(summary = "Cadastrar novo usuário",
            description = "Registra novo usuário e retorna token JWT")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        log.info("Iniciando cadastro de novo usuário", new Object[]{"email", request.getEmail(), "nome", request.getNome()});
        
        try {
            AuthenticationResponse response = authenticationService.register(request);
            log.info("Usuário cadastrado com sucesso", new Object[]{"email", request.getEmail()});
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            log.error("Erro ao cadastrar usuário", new Object[]{"email", request.getEmail(), "erro", e.getMessage()});
            throw e;
        }
    }

    /**
     * Login de usuário.
     *
     * POST /api/auth/login
     *
     * Body:
     * {
     *   "email": "joao@email.com",
     *   "senha": "senha123"
     * }
     *
     * Response (200):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "type": "Bearer",
     *   "email": "joao@email.com",
     *   "nome": "João Silva"
     * }
     *
     * Validações:
     * - Email existe
     * - Senha correta
     * - Usuário ativo
     *
     * @param request credenciais (email + senha)
     * @return response com token JWT
     */
    @PostMapping("/login")
    @Operation(summary = "Login",
            description = "Autentica usuário e retorna token JWT")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("Tentativa de login", new Object[]{"email", request.getEmail()});
        
        try {
            AuthenticationResponse response = authenticationService.login(request);
            log.info("Login bem-sucedido", new Object[]{"email", request.getEmail()});
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("Falha na autenticação", new Object[]{"email", request.getEmail(), "erro", e.getMessage()});
            throw e;
        }
    }
}