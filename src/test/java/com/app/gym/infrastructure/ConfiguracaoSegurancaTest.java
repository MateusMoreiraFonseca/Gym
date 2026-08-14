package com.app.gym.infrastructure;

import com.app.gym.application.RepositorioContaUsuario;
import com.app.gym.domain.ContaUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ConfiguracaoSegurancaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepositorioContaUsuario repositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    @BeforeEach
    void prepararUsuarios() {
        repositorio.deleteAll();
        repositorio.save(new ContaUsuario("usuario", passwordEncoder.encode("Senha123!"), false));
        repositorio.save(new ContaUsuario("admin", passwordEncoder.encode("Senha123!"), true));
    }

    @Test
    void deveCarregarUsuarioComRoleUser() {
        UserDetails usuario = userDetailsService.loadUserByUsername("usuario");

        assertEquals("usuario", usuario.getUsername());
        assertTrue(usuario.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void deveInformarQuandoUsuarioNaoExiste() {
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("ausente"));
    }

    @Test
    void loginDeUsuarioComumDeveRedirecionarParaHome() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "usuario")
                        .param("password", "Senha123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    void loginDeAdministradorDeveRedirecionarParaAreaAdministrativa() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "Senha123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/usuarios"));
    }
}
