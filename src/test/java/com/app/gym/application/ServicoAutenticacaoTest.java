package com.app.gym.application;

import com.app.gym.domain.ContaUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ServiçoAutenticacao
 * 
 * Verifica:
 * - Cadastro de usuários válidos
 * - Rejeição de usuários duplicados
 * - Criptografia de senhas com BCrypt
 * - Criação de administradores
 * - Validação de inputs
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ServiçoAutenticacao - Testes de Registro")
class ServicoAutenticacaoTest {

    @Autowired
    private ServiçoAutenticacao servicoAutenticacao;

    @Autowired
    private RepositorioContaUsuario repositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        repositorio.deleteAll();
        // Criar usuário padrão "admin" para testes de duplicação
        repositorio.save(new ContaUsuario("admin", passwordEncoder.encode("admin123"), true));
    }

    // ========== Testes de Cadastro Básico ==========

    @Test
    @DisplayName("Deve cadastrar usuário válido com sucesso")
    void testCadastroUsuarioValido() {
        // Given
        String username = "joao.silva";
        String senha = "SenhaSegura123!";
        boolean admin = false;

        // When
        ContaUsuario usuario = servicoAutenticacao.cadastrar(username, senha, admin);

        // Then
        assertNotNull(usuario);
        assertNotNull(usuario.getId());
        assertEquals(username, usuario.getUsername());
        assertFalse(usuario.isAdmin());
        assertTrue(passwordEncoder.matches(senha, usuario.getPassword()));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar usuário duplicado")
    void testCadastroUsuarioDuplicado() {
        // Given
        String usernameExistente = "admin";
        String senha = "nova_senha123";

        // When & Then
        assertThrows(UsuarioJaExistenteException.class, () ->
            servicoAutenticacao.cadastrar(usernameExistente, senha, false)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar com username vazio")
    void testCadastroComUsernameVazio() {
        // Given
        String username = "";
        String senha = "SenhaValida123!";

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            servicoAutenticacao.cadastrar(username, senha, false)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar com username null")
    void testCadastroComUsernameNull() {
        // Given
        String username = null;
        String senha = "SenhaValida123!";

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            servicoAutenticacao.cadastrar(username, senha, false)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar com senha vazia")
    void testCadastroComSenhaVazia() {
        // Given
        String username = "novo_usuario";
        String senha = "";

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            servicoAutenticacao.cadastrar(username, senha, false)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar com senha null")
    void testCadastroComSenhaNula() {
        // Given
        String username = "novo_usuario";
        String senha = null;

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            servicoAutenticacao.cadastrar(username, senha, false)
        );
    }

    // ========== Testes de Criptografia ==========

    @Test
    @DisplayName("Senha deve ser criptografada com BCrypt")
    void testSenhaEhEncriptadaComBCrypt() {
        // Given
        String username = "maria.santos";
        String senhaPlaintext = "MinhaSenha123!";

        // When
        ContaUsuario usuario = servicoAutenticacao.cadastrar(username, senhaPlaintext, false);

        // Then
        assertNotEquals(senhaPlaintext, usuario.getPassword());
        assertTrue(passwordEncoder.matches(senhaPlaintext, usuario.getPassword()));
    }

    @Test
    @DisplayName("Mesma senha deve gerar hashes diferentes (salt aleatório)")
    void testCriptografiaBCryptComSaltAleatorio() {
        // Given
        String username1 = "usuario1";
        String username2 = "usuario2";
        String mesmaSeinha = "SenhaIdentica123!";

        // When
        ContaUsuario user1 = servicoAutenticacao.cadastrar(username1, mesmaSeinha, false);
        ContaUsuario user2 = servicoAutenticacao.cadastrar(username2, mesmaSeinha, false);

        // Then
        assertNotEquals(user1.getPassword(), user2.getPassword());
        assertTrue(passwordEncoder.matches(mesmaSeinha, user1.getPassword()));
        assertTrue(passwordEncoder.matches(mesmaSeinha, user2.getPassword()));
    }

    // ========== Testes de Role Admin ==========

    @Test
    @DisplayName("Deve criar usuário com perfil de administrador")
    void testCriarUsuarioAdministrador() {
        // Given
        String username = "novo_admin";
        String senha = "AdminPass123!";

        // When
        ContaUsuario usuario = servicoAutenticacao.cadastrar(username, senha, true);

        // Then
        assertNotNull(usuario);
        assertTrue(usuario.isAdmin());
    }

    @Test
    @DisplayName("Deve criar usuário comum sem privilégios admin")
    void testCriarUsuarioComum() {
        // Given
        String username = "usuario_comum";
        String senha = "UserPass123!";

        // When
        ContaUsuario usuario = servicoAutenticacao.cadastrar(username, senha, false);

        // Then
        assertNotNull(usuario);
        assertFalse(usuario.isAdmin());
    }

    // ========== Testes de Persistência ==========

    @Test
    @DisplayName("Usuário cadastrado deve estar disponível no repositório")
    void testUsuarioPersistidoNoRepositorio() {
        // Given
        String username = "pedro.oliveira";
        String senha = "PedroPass123!";

        // When
        ContaUsuario usuarioCadastrado = servicoAutenticacao.cadastrar(username, senha, false);

        // Then
        ContaUsuario usuarioRecuperado = repositorio.findByUsername(username).orElse(null);
        assertNotNull(usuarioRecuperado);
        assertEquals(usuarioCadastrado.getId(), usuarioRecuperado.getId());
        assertEquals(username, usuarioRecuperado.getUsername());
    }

}
