package com.app.gym.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ControladorAutenticacaoTest {

    @Autowired
    private MockMvc mockMvc;

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
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }
}

