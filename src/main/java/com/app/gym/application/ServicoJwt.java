package com.app.gym.application;

import com.app.gym.domain.ContaUsuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class ServicoJwt {

    private final SecretKey chave;
    private final long tempoExpiracaoEmMilissegundos;

    /**
     * Inicializa o serviço de JWT com a chave secreta e o tempo de expiração configurados.
     */
    public ServicoJwt(@Value("${jwt.secret:chave-secreta-para-dev-1234567890}") String segredo,
                      @Value("${jwt.expiration-ms:3600000}") long tempoExpiracaoEmMilissegundos) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.tempoExpiracaoEmMilissegundos = tempoExpiracaoEmMilissegundos;
    }

    /**
     * Gera um token JWT com as informações básicas da conta de usuário.
     */
    public String gerarToken(ContaUsuario contaUsuario) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + tempoExpiracaoEmMilissegundos);

        return Jwts.builder()
                .subject(contaUsuario.getUsername())
                .claim("admin", contaUsuario.isAdmin())
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(chave)
                .compact();
    }

    /**
     * Extrai o nome de usuário a partir do payload do token.
     */
    public String extrairNomeUsuario(String token) {
        return extrairClaim(token, Claims::getSubject);
    }

    /**
     * Valida se o token informado ainda é válido e foi assinado corretamente.
     */
    public boolean tokenValido(String token) {
        try {
            Jwts.parser()
                    .verifyWith(chave)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Extrai uma claim específica do token a partir de uma função de leitura.
     */
    private <T> T extrairClaim(String token, Function<Claims, T> extrator) {
        Claims claims = Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return extrator.apply(claims);
    }
}
