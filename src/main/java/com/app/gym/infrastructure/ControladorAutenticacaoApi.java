package com.app.gym.infrastructure;

import com.app.gym.application.RepositorioContaUsuario;
import com.app.gym.application.ServicoJwt;
import com.app.gym.domain.ContaUsuario;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class ControladorAutenticacaoApi {

    private final RepositorioContaUsuario repositorioContaUsuario;
    private final PasswordEncoder passwordEncoder;
    private final ServicoJwt servicoJwt;

    /**
     * Cria o controlador de autenticação para a API com os componentes necessários para validar credenciais e gerar tokens.
     */
    public ControladorAutenticacaoApi(RepositorioContaUsuario repositorioContaUsuario,
                                      PasswordEncoder passwordEncoder,
                                      ServicoJwt servicoJwt) {
        this.repositorioContaUsuario = repositorioContaUsuario;
        this.passwordEncoder = passwordEncoder;
        this.servicoJwt = servicoJwt;
    }

    /**
     * Valida as credenciais recebidas e retorna um token JWT quando o login for bem-sucedido.
     */
    @PostMapping("/login")
    public ResponseEntity<?> realizarLogin(@RequestBody CredenciaisLogin credenciaisLogin) {
        Optional<ContaUsuario> contaUsuarioOpcional = repositorioContaUsuario.findByUsername(credenciaisLogin.username());

        if (contaUsuarioOpcional.isPresent() && passwordEncoder.matches(credenciaisLogin.password(), contaUsuarioOpcional.get().getPassword())) {
            ContaUsuario contaUsuario = contaUsuarioOpcional.get();
            String token = servicoJwt.gerarToken(contaUsuario);
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "tokenType", "Bearer"
            ));
        }

        return ResponseEntity.status(401).body(Map.of("erro", "Credenciais inválidas"));
    }

    public record CredenciaisLogin(String username, String password) {
    }
}
