package com.app.gym.infrastructure;

import com.app.gym.application.RepositorioContaUsuario;
import com.app.gym.application.ServicoMedicao;
import com.app.gym.domain.ContaUsuario;
import com.app.gym.domain.Medicao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.security.core.Authentication;

@Controller
public class ControladorMedicoes {
    private final RepositorioContaUsuario repositorioContaUsuario;
    private final ServicoMedicao servicoMedicao;

    public ControladorMedicoes(RepositorioContaUsuario repositorioContaUsuario, ServicoMedicao servicoMedicao) {
        this.repositorioContaUsuario = repositorioContaUsuario;
        this.servicoMedicao = servicoMedicao;
    }

    @GetMapping("/medicoes")
    public String paginaMedicoes(Authentication authentication, Model model) {
        ContaUsuario usuario = obterUsuario(authentication);
        model.addAttribute("medicoes", servicoMedicao.listar(usuario));
        model.addAttribute("formulario", new FormularioMedicao());
        return "medicoes";
    }

    @PostMapping("/medicoes")
    public String registrar(
            Authentication authentication,
            @Valid @ModelAttribute("formulario") FormularioMedicao formulario,
            BindingResult resultado,
            Model model) {
        ContaUsuario usuario = obterUsuario(authentication);
        model.addAttribute("medicoes", servicoMedicao.listar(usuario));

        if (resultado.hasErrors()) {
            model.addAttribute("errorMessage", "Informe peso e altura válidos.");
            return "medicoes";
        }

        try {
            servicoMedicao.registrar(usuario, formulario.getPeso(), formulario.getAltura());
            return "redirect:/medicoes";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            return "medicoes";
        }
    }

    private ContaUsuario obterUsuario(Authentication authentication) {
        return repositorioContaUsuario.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário logado não encontrado"));
    }

    public static class FormularioMedicao {
        @NotNull
        @DecimalMin("0.1")
        @DecimalMax("500")
        private Double peso;

        @NotNull
        @DecimalMin("0.5")
        @DecimalMax("3")
        private Double altura;

        public Double getPeso() {
            return peso;
        }

        public void setPeso(Double peso) {
            this.peso = peso;
        }

        public Double getAltura() {
            return altura;
        }

        public void setAltura(Double altura) {
            this.altura = altura;
        }
    }
}
