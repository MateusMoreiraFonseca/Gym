package com.app.gym.infrastructure;

import com.app.gym.application.RepositorioContaUsuario;
import com.app.gym.application.ServicoJwt;
import com.app.gym.domain.ContaUsuario;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class FiltroAutenticacaoJwtTest {

    private final ServicoJwt servicoJwt = mock(ServicoJwt.class);
    private final RepositorioContaUsuario repositorio = mock(RepositorioContaUsuario.class);
    private final UserDetailsService usuarios = mock(UserDetailsService.class);
    private final FilterChain cadeia = mock(FilterChain.class);
    private final FiltroAutenticacaoJwt filtro = new FiltroAutenticacaoJwt(servicoJwt, repositorio, usuarios);

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAutenticarUsuarioComBearerTokenValido() throws Exception {
        MockHttpServletRequest requisicao = requisicaoComToken("token-valido");
        ContaUsuario conta = new ContaUsuario("ana", "hash", false);
        User usuario = new User("ana", "hash", java.util.List.of());
        when(servicoJwt.tokenValido("token-valido")).thenReturn(true);
        when(servicoJwt.extrairNomeUsuario("token-valido")).thenReturn("ana");
        when(repositorio.findByUsername("ana")).thenReturn(Optional.of(conta));
        when(usuarios.loadUserByUsername("ana")).thenReturn(usuario);

        filtro.doFilterInternal(requisicao, new MockHttpServletResponse(), cadeia);

        assertEquals("ana", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(cadeia).doFilter(any(), any());
    }

    @Test
    void deveIgnorarCabecalhoAusente() throws Exception {
        filtro.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(), cadeia);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(servicoJwt, repositorio, usuarios);
        verify(cadeia).doFilter(any(), any());
    }

    @Test
    void deveIgnorarCabecalhoSemPrefixoBearer() throws Exception {
        MockHttpServletRequest requisicao = new MockHttpServletRequest();
        requisicao.addHeader("Authorization", "token-sem-prefixo");

        filtro.doFilterInternal(requisicao, new MockHttpServletResponse(), cadeia);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(servicoJwt, repositorio, usuarios);
    }

    @Test
    void naoDeveAutenticarTokenInvalido() throws Exception {
        when(servicoJwt.tokenValido("invalido")).thenReturn(false);

        filtro.doFilterInternal(requisicaoComToken("invalido"), new MockHttpServletResponse(), cadeia);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(servicoJwt, never()).extrairNomeUsuario(any());
        verifyNoInteractions(repositorio, usuarios);
    }

    @Test
    void naoDeveAutenticarQuandoUsuarioDoTokenNaoExiste() throws Exception {
        when(servicoJwt.tokenValido("token")).thenReturn(true);
        when(servicoJwt.extrairNomeUsuario("token")).thenReturn("ausente");
        when(repositorio.findByUsername("ausente")).thenReturn(Optional.empty());

        filtro.doFilterInternal(requisicaoComToken("token"), new MockHttpServletResponse(), cadeia);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(usuarios);
    }

    private MockHttpServletRequest requisicaoComToken(String token) {
        MockHttpServletRequest requisicao = new MockHttpServletRequest();
        requisicao.addHeader("Authorization", "Bearer " + token);
        return requisicao;
    }
}
