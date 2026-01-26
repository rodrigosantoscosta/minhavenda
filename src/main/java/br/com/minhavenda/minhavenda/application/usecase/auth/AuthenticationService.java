package br.com.minhavenda.minhavenda.application.usecase.auth;

import br.com.minhavenda.minhavenda.application.dto.auth.AuthenticationResponse;
import br.com.minhavenda.minhavenda.application.dto.auth.LoginRequest;
import br.com.minhavenda.minhavenda.application.dto.auth.RegisterRequest;
import br.com.minhavenda.minhavenda.domain.entity.Usuario;
import br.com.minhavenda.minhavenda.domain.enums.TipoUsuario;
import br.com.minhavenda.minhavenda.infrastructure.persistence.repository.UsuarioRepository;
import br.com.minhavenda.minhavenda.infrastructure.security.JwtService;
import br.com.minhavenda.minhavenda.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de Autenticação.
 * 
 * Responsabilidades:
 * - Registrar novos usuários
 * - Validar email e CPF únicos
 * - Hash de senhas com BCrypt
 * - Login de usuários
 * - Gerar tokens JWT
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    /**
     * Registra novo usuário no sistema.
     * 
     * Fluxo:
     * 1. Valida se email já existe
     * 2. Valida se CPF já existe
     * 3. Remove máscara do CPF
     * 4. Hash da senha com BCrypt
     * 5. Cria usuário
     * 6. Salva no banco
     * 7. Gera token JWT
     * 8. Retorna response com token
     * 
     * @param request dados do novo usuário
     * @return response com token JWT
     * @throws RuntimeException se email ou CPF já existem
     */
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        
        // 1. VALIDA EMAIL ÚNICO
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado: " + request.getEmail());
        }

        // 2. VALIDA CPF ÚNICO
        // Remove máscara do CPF para comparação
//        String cpfSemMascara = request.getCpf().replaceAll("[^0-9]", "");
//
//        if (usuarioRepository.existsByCpf(cpfSemMascara)) {
//            throw new RuntimeException("CPF já cadastrado");
//        }

        // 3. HASH DA SENHA COM BCRYPT
        // BCrypt adiciona "salt" automático
        // Mesmo senhas iguais geram hashs diferentes
        String senhaHash = passwordEncoder.encode(request.getSenha());

        // 4. CRIA USUÁRIO
        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
//                .cpf(cpfSemMascara)  // Salva sem máscara
                .senha(senhaHash)     // Salva senha hasheada
//                .telefone(request.getTelefone())
                .tipo(TipoUsuario.CLIENTE)  // Novo usuário sempre é CLIENTE
                .ativo(true)
                .build();

        // 5. SALVA NO BANCO
        usuarioRepository.save(usuario);

        // 6. GERA TOKEN JWT
        // Carrega UserDetails para gerar token
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String jwtToken = jwtService.generateToken(userDetails);

        // 7. RETORNA RESPONSE
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .email(usuario.getEmail())
                .nome(usuario.getNome())
                .build();
    }

    /**
     * Autentica usuário (login).
     * 
     * Fluxo:
     * 1. Valida credenciais (email + senha)
     * 2. Se válido, busca usuário
     * 3. Gera token JWT
     * 4. Retorna response com token
     * 
     * @param request credenciais (email + senha)
     * @return response com token JWT
     * @throws RuntimeException se credenciais inválidas
     */
    @Transactional(readOnly = true)
    public AuthenticationResponse login(LoginRequest request) {
        
        // 1. VALIDA CREDENCIAIS
        // AuthenticationManager valida email + senha
        // Se inválido, lança exceção automática
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getSenha()
                )
        );

        // 2. SE CHEGOU AQUI, CREDENCIAIS SÃO VÁLIDAS
        // Busca usuário do banco
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 3. GERA TOKEN JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String jwtToken = jwtService.generateToken(userDetails);

        // 4. RETORNA RESPONSE
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .email(usuario.getEmail())
                .nome(usuario.getNome())
                .build();
    }
}
