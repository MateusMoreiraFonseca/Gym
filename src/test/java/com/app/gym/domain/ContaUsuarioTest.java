// package com.app.gym.domain;

// import org.junit.jupiter.api.Test;
// import static org.junit.jupiter.api.Assertions.*;

// public class ContaUsuarioTest {

//     @Test
//     public void deveCriarContaUsuarioComDadosValidos() {
//         // Given
//         String nome = "João Silva";
//         String email = "joao.silva@example.com";
//         String senha = "senhaSegura123";

//         // When
//         ContaUsuario conta = new ContaUsuario(nome, email, senha);

//         // Then
//         assertEquals(nome, conta.getNome());
//         assertEquals(email, conta.getEmail());
//         assertNotNull(conta.getId());
//         assertTrue(conta.isAtiva());
//     }

//     @Test
//     public void deveValidarEmailInvalido() {
//         // Given
//         String nome = "João Silva";
//         String email = "emailinvalido";
//         String senha = "senhaSegura123";

//         // When & Then
//         assertThrows(IllegalArgumentException.class, () -> {
//             new ContaUsuario(nome, email, senha);
//         });
//     }

//     @Test
//     public void deveValidarSenhaFraca() {
//         // Given
//         String nome = "João Silva";
//         String email = "joao.silva@example.com";
//         String senha = "123";

//         // When & Then
//         assertThrows(IllegalArgumentException.class, () -> {
//             new ContaUsuario(nome, email, senha);
//         });
//     }
// }