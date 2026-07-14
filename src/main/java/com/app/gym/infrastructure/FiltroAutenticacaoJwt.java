package com.app.gym.infrastructure;

import com.app.gym.application.RepositorioContaUsuario;
import com.app.gym.application.ServicoJwt;
import com.app.gym.domain.ContaUsuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

public class FiltroAutenticacaoJwt extends OncePerRequestFilter {

    private final ServicoJwt servicoJwt;
    private final RepositorioContaUsuario repositorioContaUsuario;
    private final UserDetailsService userDetailsService;

    /**
     * Cria o filtro com as dependências necessárias para validar tokens e carregar o usuário autenticado.
     */
    public FiltroAutenticacaoJwt(ServicoJwt servicoJwt,
                                 RepositorioContaUsuario repositorioContaUsuario,
                                 UserDetailsService userDetailsService) {
        this.servicoJwt = servicoJwt;
        this.repositorioContaUsuario = repositorioContaUsuario;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Processa o cabeçalho Authorization e estabelece a autenticação quando um token JWT válido for encontrado.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String cabecalhoAutorizacao = request.getHeader("Authorization");

        if (cabecalhoAutorizacao != null && cabecalhoAutorizacao.startsWith("Bearer ")) {
            String token = cabecalhoAutorizacao.substring(7);
            if (servicoJwt.tokenValido(token)) {
                String nomeUsuario = servicoJwt.extrairNomeUsuario(token);
                Optional<ContaUsuario> contaUsuario = repositorioContaUsuario.findByUsername(nomeUsuario);

                if (contaUsuario.isPresent()) {
                    User usuarioSpring = (User) userDetailsService.loadUserByUsername(nomeUsuario);
                    UsernamePasswordAuthenticationToken autenticacao = new UsernamePasswordAuthenticationToken(
                            usuarioSpring,
                            null,
                            usuarioSpring.getAuthorities());
                    autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(autenticacao);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
