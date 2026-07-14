package com.app.gym.infrastructure;

import com.app.gym.application.RepositorioContaUsuario;
import com.app.gym.application.ServicoListarUsuarios;
import com.app.gym.domain.ContaUsuario;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ControladorUsuariosAdmin {

    private final ServicoListarUsuarios servicoListarUsuarios;
    private final RepositorioContaUsuario repositorioContaUsuario;


    /**
     * Cria o controlador para telas administrativas.
     */
    public ControladorUsuariosAdmin(ServicoListarUsuarios servicoListarUsuarios,
                                     RepositorioContaUsuario repositorioContaUsuario) {
        this.servicoListarUsuarios = servicoListarUsuarios;
        this.repositorioContaUsuario = repositorioContaUsuario;
    }


    /**
     * Exibe uma tela com a lista de usuários existentes (acesso restrito a ADMIN).
     */
    @GetMapping("/admin/usuarios")
    public String listarUsuarios(Model model, Authentication authentication) {
        List<ContaUsuario> usuarios = servicoListarUsuarios.listarTodosUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "admin-usuarios";
    }

    @PostMapping("/admin/usuarios/deletar")
    public String deletarUsuario(
            @RequestParam("usuarioId") Long usuarioId,
            Model model,
            Authentication authentication
    ) {
        boolean logadoEhAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!logadoEhAdmin) {
            model.addAttribute("errorMessage", "Acesso negado.");
            model.addAttribute("usuarios", servicoListarUsuarios.listarTodosUsuarios());
            return "admin-usuarios";
        }

        // Como o enunciado pede uma regra adicional: o usuário selecionado NÃO pode ser admin.
        // Para isso, precisamos buscar o usuário a ser deletado.
        var usuarioADeletar = servicoListarUsuarios.listarTodosUsuarios().stream()
                .filter(u -> u.getId().equals(usuarioId))
                .findFirst();

        if (usuarioADeletar.isEmpty()) {
            model.addAttribute("errorMessage", "Usuário não encontrado.");
            model.addAttribute("usuarios", servicoListarUsuarios.listarTodosUsuarios());
            return "admin-usuarios";
        }

        if (usuarioADeletar.get().isAdmin()) {
            model.addAttribute("errorMessage", "Não é permitido deletar um usuário ADMIN.");
            model.addAttribute("usuarios", servicoListarUsuarios.listarTodosUsuarios());
            return "admin-usuarios";
        }

        // Regra atendida: deletar usuário (apenas USERs).
        repositorioContaUsuario.deleteById(usuarioId);

        model.addAttribute("successMessage", "Usuário deletado com sucesso.");
        model.addAttribute("usuarios", servicoListarUsuarios.listarTodosUsuarios());
        return "admin-usuarios";
    }


    // Endpoint /admin/usuarios/novo removido: criação de usuários agora reutiliza /register.
    // O form de admin em /register aparece somente para quem já está com ROLE_ADMIN.


    public static class FormularioNovoUsuario {

        private String username;
        private String password;
        private boolean admin;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isAdmin() {
            return admin;
        }

        public void setAdmin(boolean admin) {
            this.admin = admin;
        }
    }

}

