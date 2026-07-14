package com.app.gym.infrastructure;

import com.app.gym.application.RepositorioContaUsuario;
import com.app.gym.application.ServicoCriptografiaSenha;
import com.app.gym.domain.ContaUsuario;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Objects;

@Controller
public class ControladorPerfil {

    private final RepositorioContaUsuario repositorioContaUsuario;
    private final com.app.gym.application.ServicoCriptografiaSenha servicoCriptografiaSenha;

    public ControladorPerfil(RepositorioContaUsuario repositorioContaUsuario, ServicoCriptografiaSenha servicoCriptografiaSenha) {
        this.repositorioContaUsuario = repositorioContaUsuario;
        this.servicoCriptografiaSenha = servicoCriptografiaSenha;
    }

    @GetMapping("/perfil/editar")
    public String paginaEditarPerfil(Authentication authentication, Model model) {
        String username = authentication.getName();
        ContaUsuario usuario = repositorioContaUsuario.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuário logado não encontrado"));

        model.addAttribute("username", usuario.getUsername());
        model.addAttribute("formulario", new FormularioEditarPerfil());
        return "editar-perfil";
    }

    @PostMapping("/perfil/editar")
    public String editarPerfil(
            Authentication authentication,
            @ModelAttribute("formulario") FormularioEditarPerfil formulario,
            Model model
    ) {
        String username = authentication.getName();
        ContaUsuario usuario = repositorioContaUsuario.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuário logado não encontrado"));

        model.addAttribute("username", usuario.getUsername());

        if (!Objects.equals(formulario.getNovaSenha(), formulario.getConfirmacaoNovaSenha())) {
            model.addAttribute("errorMessage", "As senhas não conferem.");
            return "editar-perfil";
        }

        if (formulario.getNovaSenha() == null || formulario.getNovaSenha().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Informe uma nova senha.");
            return "editar-perfil";
        }

        String novaSenhaCriptografada = servicoCriptografiaSenha.criptografar(formulario.getNovaSenha());
        usuario = new ContaUsuario(usuario.getUsername(), novaSenhaCriptografada, usuario.isAdmin());

        // Preserva o id para update
        try {
            var idField = ContaUsuario.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(usuario, repositorioContaUsuario.findByUsername(username).orElseThrow().getId());
        } catch (Exception ignored) {
            // fallback abaixo
        }

        repositorioContaUsuario.save(usuario);

        model.addAttribute("successMessage", "Senha atualizada com sucesso.");
        return "editar-perfil";
    }

    public static class FormularioEditarPerfil {
        private String novaSenha;
        private String confirmacaoNovaSenha;

        public String getNovaSenha() {
            return novaSenha;
        }

        public void setNovaSenha(String novaSenha) {
            this.novaSenha = novaSenha;
        }

        public String getConfirmacaoNovaSenha() {
            return confirmacaoNovaSenha;
        }

        public void setConfirmacaoNovaSenha(String confirmacaoNovaSenha) {
            this.confirmacaoNovaSenha = confirmacaoNovaSenha;
        }
    }
}

