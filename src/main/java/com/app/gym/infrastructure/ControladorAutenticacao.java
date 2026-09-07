package com.app.gym.infrastructure;

import com.app.gym.application.ServiçoAutenticacao;
import com.app.gym.application.UsuarioJaExistenteException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ControladorAutenticacao {
    private final ServiçoAutenticacao authService;

    /**
     * Cria o controlador com o serviço de autenticação responsável por registrar os usuários.
     */
    public ControladorAutenticacao(ServiçoAutenticacao authService) {
        this.authService = authService;
    }

    /**
     * Exibe a página de login para que o usuário possa autenticar-se.
     */
    @GetMapping("/login")
    public String paginaLogin() {
        return "login";
    }

    /**
     * Exibe a tela de cadastro com um formulário vazio preparado para preenchimento.
     */
    @GetMapping("/register")
    public String paginaCadastro(Model model) {
        model.addAttribute("userForm", new FormularioCadastro());
        return "register";
    }

    /**
     * Processa o cadastro de um novo usuário e redireciona para a tela de login.
     */
    @PostMapping("/register")
    public String cadastrar(@Valid @ModelAttribute("userForm") FormularioCadastro form,
                            BindingResult resultadoValidacao,
                            Model model) {
        if (resultadoValidacao.hasErrors()) {
            model.addAttribute("errorMessage", "Informe usuário e senha.");
            return "register";
        }

        try {
            // Segurança: mesmo que o formulário seja manipulado no navegador, apenas ADMIN pode criar outro admin.
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            boolean isAdminLogado = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isAdminCriar = isAdminLogado && form.isAdmin();

            authService.cadastrar(form.getUsername(), form.getPassword(), isAdminCriar);
            return "redirect:/home";



        } catch (UsuarioJaExistenteException ex) {
            model.addAttribute("userForm", form);
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }
    }



    /**
     * Exibe a página inicial após a autenticação bem-sucedida do usuário.
     */
    public String paginaInicial() {
        return "home";
    }

    public static class FormularioCadastro {
        @NotBlank
        private String username;

        @NotBlank
        private String password;

        // Só admins enxergam isso no HTML; backend garante regra.
        private boolean isAdmin;

        /**
         * Cria um formulário de cadastro vazio.
         */
        public FormularioCadastro() {
        }

        /**
         * Cria um formulário de cadastro com os dados informados.
         */
        public FormularioCadastro(String username, String password) {
            this.username = username;
            this.password = password;
        }

        /**
         * Retorna o nome de usuário informado no formulário.
         */
        public String getUsername() {
            return username;
        }

        /**
         * Atualiza o nome de usuário informado no formulário.
         */
        public void setUsername(String username) {
            this.username = username;
        }

        /**
         * Retorna a senha informada no formulário.
         */
        public String getPassword() {
            return password;
        }

        /**
         * Atualiza a senha informada no formulário.
         */
        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isAdmin() {
            return isAdmin;
        }

        public void setAdmin(boolean admin) {
            this.isAdmin = admin;
        }
    }
}
