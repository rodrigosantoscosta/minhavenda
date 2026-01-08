package br.com.minhavenda.minhavenda.presentation.controller;

import br.com.minhavenda.minhavenda.application.dto.UsuarioDTO;
import br.com.minhavenda.minhavenda.application.usecase.usuario.BuscarPerfilUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para gerenciar perfil do usuário logado.
 *
 * Endpoints:
 * - GET /perfil - Retorna dados do usuário logado
 *
 * Todos os endpoints requerem autenticação (token JWT).
 */
@RestController
@RequestMapping("/perfil")
@RequiredArgsConstructor
@Tag(name = "Perfil", description = "Gerenciamento do perfil do usuário logado")
@SecurityRequirement(name = "bearer-auth") // Para Swagger
public class PerfilController {

    private final BuscarPerfilUseCase buscarPerfilUseCase;

    /**
     * Retorna os dados do usuário logado.
     *
     * GET /api/perfil
     * Header: Authorization: Bearer <token>
     *
     * Response (200):
     * {
     *   "id": "uuid",
     *   "nome": "João Silva",
     *   "email": "joao@email.com",
     *   "tipo": "CLIENTE",
     *   "ativo": true,
     *   "dataCadastro": "2026-01-08T..."
     * }
     *
     * @param authentication objeto de autenticação (injetado automaticamente)
     * @return dados do usuário logado
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Buscar perfil do usuário logado",
            description = "Retorna os dados do usuário autenticado (sem senha)"
    )
    public ResponseEntity<UsuarioDTO> buscarPerfil(Authentication authentication) {
        // Extrai email do usuário logado
        String email = authentication.getName();

        // Busca dados completos do usuário
        UsuarioDTO usuario = buscarPerfilUseCase.executar(email);

        return ResponseEntity.ok(usuario);
    }
}