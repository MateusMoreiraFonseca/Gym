package com.app.gym.infrastructure;

import com.app.gym.application.RepositorioContaUsuario;
import com.app.gym.domain.ContaUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ControladorAutenticacaoApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepositorioContaUsuario repositorioContaUsuario;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void configurar() {
        repositorioContaUsuario.deleteAll();
        repositorioContaUsuario.save(new ContaUsuario("usuarioTeste", passwordEncoder.encode("senha123"), false));
    }

    @Test
    void loginDeveRetornarTokenJwt() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"usuarioTeste\",\"password\":\"senha123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tokenType", org.hamcrest.Matchers.is("Bearer")));
    }

    @Test
    void loginComSenhaInvalidaDeveRetornarNaoAutorizado() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"usuarioTeste\",\"password\":\"senha-errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro", org.hamcrest.Matchers.is("Credenciais inválidas")));
    }

    @Test
    void loginComUsuarioInexistenteDeveRetornarNaoAutorizado() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"inexistente\",\"password\":\"senha\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginComCamposVaziosDeveRetornarBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\" \",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginComJsonMalformadoDeveRetornarBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"usuarioTeste\""))
                .andExpect(status().isBadRequest());
    }
}
