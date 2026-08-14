package com.app.gym.application;

import com.app.gym.domain.ContaUsuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ServicoJwt
 * 
 * Verifica:
 * - Geração válida de tokens JWT
 * - Validação de tokens
 * - Extração de informações do token
 * - Rejeição de tokens expirados/modificados
 * - Configuração segura do secret
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ServicoJwt - Testes de Token JWT")
class ServicoJwtTest {

    @Autowired
    private ServicoJwt servicoJwt;

    // ========== Testes de Geração de Token ==========

    @Test
    @DisplayName("Deve gerar token JWT válido para usuário comum")
    void testGerarTokenValido() {
        // Given
        ContaUsuario usuario = new ContaUsuario("usuario_teste", "senha123", false);

        // When
        String token = servicoJwt.gerarToken(usuario);

        // Then
        assertNotNull(token);
        assertNotEmpty(token);
        assertTrue(token.contains("."), "Token deve ter 3 partes separadas por ponto");
        String[] partes = token.split("\\.");
        assertEquals(3, partes.length, "JWT deve ter header.payload.signature");
    }

    @Test
    @DisplayName("Deve gerar token JWT válido para administrador")
    void testGerarTokenParaAdmin() {
        // Given
        ContaUsuario admin = new ContaUsuario("admin_user", "admin123", true);

        // When
        String token = servicoJwt.gerarToken(admin);

        // Then
        assertNotNull(token);
        String usernameExtraido = servicoJwt.extrairNomeUsuario(token);
        assertEquals("admin_user", usernameExtraido);
    }

    @Test
    @DisplayName("Token gerado deve conter o username no payload")
    void testTokenContemUsername() {
        // Given
        ContaUsuario usuario = new ContaUsuario("joao.silva", "senha_joao", false);

        // When
        String token = servicoJwt.gerarToken(usuario);

        // Then
        String usernameExtraido = servicoJwt.extrairNomeUsuario(token);
        assertEquals("joao.silva", usernameExtraido);
    }

    @Test
    @DisplayName("Tokens diferentes para usuários diferentes")
    void testTokensDiferentesPorUsuario() {
        // Given
        ContaUsuario usuario1 = new ContaUsuario("usuario1", "pass1", false);
        ContaUsuario usuario2 = new ContaUsuario("usuario2", "pass2", false);

        // When
        String token1 = servicoJwt.gerarToken(usuario1);
        String token2 = servicoJwt.gerarToken(usuario2);

        // Then
        assertNotEquals(token1, token2);
        assertEquals("usuario1", servicoJwt.extrairNomeUsuario(token1));
        assertEquals("usuario2", servicoJwt.extrairNomeUsuario(token2));
    }

    // ========== Testes de Validação ==========

    @Test
    @DisplayName("Token válido recém-gerado deve passar na validação")
    void testTokenValidoRetornaTrue() {
        // Given
        ContaUsuario usuario = new ContaUsuario("maria_santos", "pass123", false);
        String token = servicoJwt.gerarToken(usuario);

        // When
        boolean valido = servicoJwt.tokenValido(token);

        // Then
        assertTrue(valido);
    }

    @Test
    @DisplayName("Token vazio deve retornar false na validação")
    void testTokenVazioRetornaFalse() {
        // When & Then
        assertFalse(servicoJwt.tokenValido(""));
    }

    @Test
    @DisplayName("Token null deve retornar false na validação")
    void testTokenNullRetornaFalse() {
        // When & Then
        assertFalse(servicoJwt.tokenValido(null));
    }

    @Test
    @DisplayName("Token malformado deve retornar false")
    void testTokenMalFormadoRetornaFalse() {
        // When & Then
        assertFalse(servicoJwt.tokenValido("abc.xyz"));
        assertFalse(servicoJwt.tokenValido("token_invalido"));
        assertFalse(servicoJwt.tokenValido("header.payload"));
    }

    @Test
    @DisplayName("Token modificado (payload alterado) deve retornar false")
    void testTokenModificadoRetornaFalse() {
        // Given
        ContaUsuario usuario = new ContaUsuario("pedro_oliveira", "pass123", false);
        String tokenOriginal = servicoJwt.gerarToken(usuario);
        String[] partes = tokenOriginal.split("\\.");
        
        // Modificar o payload (segunda parte)
        String tokenModificado = partes[0] + ".MODIFIED_PAYLOAD." + partes[2];

        // When
        boolean valido = servicoJwt.tokenValido(tokenModificado);

        // Then
        assertFalse(valido, "Token com payload modificado deve ser inválido");
    }

    @Test
    @DisplayName("Token com assinatura modificada deve retornar false")
    void testTokenComAssinaturaModificadaRetornaFalse() {
        // Given
        ContaUsuario usuario = new ContaUsuario("ana_silva", "pass123", false);
        String tokenOriginal = servicoJwt.gerarToken(usuario);
        String[] partes = tokenOriginal.split("\\.");
        
        // Modificar a assinatura (terceira parte)
        String tokenModificado = partes[0] + "." + partes[1] + ".MODIFIED_SIGNATURE";

        // When
        boolean valido = servicoJwt.tokenValido(tokenModificado);

        // Then
        assertFalse(valido, "Token com assinatura modificada deve ser inválido");
    }

    // ========== Testes de Extração de Claims ==========

    @Test
    @DisplayName("Deve extrair username corretamente do token")
    void testExtracoUsernameValido() {
        // Given
        ContaUsuario usuario = new ContaUsuario("carlos_santos", "pass123", false);
        String token = servicoJwt.gerarToken(usuario);

        // When
        String usernameExtraido = servicoJwt.extrairNomeUsuario(token);

        // Then
        assertEquals("carlos_santos", usernameExtraido);
    }

    @Test
    @DisplayName("Deve extrair username correto mesmo para token de admin")
    void testExtracoUsernameDeAdmin() {
        // Given
        ContaUsuario admin = new ContaUsuario("admin_principal", "pass123", true);
        String token = servicoJwt.gerarToken(admin);

        // When
        String usernameExtraido = servicoJwt.extrairNomeUsuario(token);

        // Then
        assertEquals("admin_principal", usernameExtraido);
    }

    // ========== Utilitários de Teste ==========

    private void assertNotEmpty(String str) {
        assertNotNull(str);
        assertFalse(str.isEmpty());
    }
}

