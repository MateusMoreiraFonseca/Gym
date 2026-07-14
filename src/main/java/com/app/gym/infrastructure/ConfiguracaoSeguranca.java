package com.app.gym.infrastructure;

import com.app.gym.application.RepositorioContaUsuario;
import com.app.gym.application.ServicoJwt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class ConfiguracaoSeguranca {

    /**
     * Cria o codificador de senhas usado para proteger as credenciais dos usuários.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Define como o sistema carrega os detalhes do usuário a partir do repositório de contas.
     */
    @Bean
    public UserDetailsService userDetailsService(RepositorioContaUsuario repository) {
        return username -> repository.findByUsername(username)
                .map(user -> User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.isAdmin() ? "ADMIN" : "USER")
                        .build())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Usuário não encontrado"));
    }


    /**
     * Cria o filtro de autenticação JWT que será incorporado à cadeia de segurança.
     */
    @Bean
    public FiltroAutenticacaoJwt filtroAutenticacaoJwt(ServicoJwt servicoJwt,
                                                       RepositorioContaUsuario repositorioContaUsuario,
                                                       UserDetailsService userDetailsService) {
        return new FiltroAutenticacaoJwt(servicoJwt, repositorioContaUsuario, userDetailsService);
    }

    /**
     * Configura as regras de acesso, login, logout e integração com o filtro JWT.
     */
    @Bean
    public SecurityFilterChain filtroSeguranca(HttpSecurity http,
                                               FiltroAutenticacaoJwt filtroAutenticacaoJwt) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/h2-console/**", "/api/auth/login").permitAll()
                .requestMatchers("/admin/usuarios").hasRole("ADMIN")
                .requestMatchers("/admin/usuarios/deletar").hasRole("ADMIN")

                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    boolean ehAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    if (ehAdmin) {
                        response.sendRedirect("/admin/usuarios");
                    } else {
                        response.sendRedirect("/home");
                    }
                })
                .permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/auth/login"));


        http.addFilterBefore(filtroAutenticacaoJwt, UsernamePasswordAuthenticationFilter.class);
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
