package com.app.gym.infrastructure;

import com.app.gym.application.RepositorioContaUsuario;
import com.app.gym.domain.ContaUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@AutoConfigureMockMvc
class ControladorAutenticacaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepositorioContaUsuario repositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepararUsuarios() {
        repositorio.deleteAll();
        repositorio.save(new ContaUsuario("admin", passwordEncoder.encode("A1b2C3!"), true));
    }

    @Test
    void registerShouldCreateUserAndRedirectToHome() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "newadmin")
                        .param("password", "A1b2C3!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    void registerShouldShowErrorWhenUserAlreadyExists() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "A1b2C3!"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("errorMessage", "Usuário já existe"));
    }

    @Test
    void registerComCamposEmBrancoDeveExibirErroDeValidacao() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", " ")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("errorMessage", "Informe usuário e senha."));
    }

    @Test
    void usuarioComumNaoPodeCriarOutroAdministradorPeloFormulario() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "tentativa-admin")
                        .param("password", "A1b2C3!")
                        .param("admin", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        ContaUsuario usuario = repositorio.findByUsername("tentativa-admin").orElseThrow();
        assertFalse(usuario.isAdmin());
    }
}

