package com.app.gym.infrastructure;

import com.app.gym.application.RepositorioContaUsuario;
import com.app.gym.application.ServicoCalorico;
import com.app.gym.domain.ContaUsuario;
import com.app.gym.domain.NivelAtividade;
import com.app.gym.domain.PerfilFitness;
import com.app.gym.domain.RegistroCalorico;
import com.app.gym.domain.Sexo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;

@Controller
public class ControladorCalorias {
    private final RepositorioContaUsuario repositorioContaUsuario;
    private final ServicoCalorico servicoCalorico;

    public ControladorCalorias(RepositorioContaUsuario repositorioContaUsuario, ServicoCalorico servicoCalorico) {
        this.repositorioContaUsuario = repositorioContaUsuario;
        this.servicoCalorico = servicoCalorico;
    }

    @GetMapping("/calorias")
    public String pagina(Authentication authentication, Model model) {
        prepararModelo(authentication, model, LocalDate.now());
        return "calorias";
    }

    @GetMapping("/perfil/fitness")
    public String perfilFitness(Authentication authentication, Model model) {
        return "redirect:/perfil";
    }

    @PostMapping("/calorias/perfil")
    public String salvarPerfil(Authentication authentication, @Valid @ModelAttribute("perfilForm") PerfilForm form,
                               BindingResult resultado, Model model) {
        if (resultado.hasErrors()) {
            prepararModelo(authentication, model, LocalDate.now());
            model.addAttribute("errorMessage", "Preencha todos os dados do perfil.");
            return "calorias";
        }
        try {
            servicoCalorico.salvarPerfil(obterUsuario(authentication), form.idade, form.altura, form.pesoAtual,
                    form.pesoMeta, form.sexo, form.nivelAtividade);
            return "redirect:/perfil";
        } catch (IllegalArgumentException exception) {
            prepararModelo(authentication, model, LocalDate.now());
            model.addAttribute("errorMessage", exception.getMessage());
            return "calorias";
        }
    }

    @PostMapping("/calorias/registro")
    public String salvarRegistro(Authentication authentication, @Valid @ModelAttribute("registroForm") RegistroForm form,
                                 BindingResult resultado, Model model) {
        LocalDate data = form.data == null ? LocalDate.now() : form.data;
        if (resultado.hasErrors()) {
            prepararModelo(authentication, model, data);
            model.addAttribute("errorMessage", "Informe calorias válidas.");
            return "calorias";
        }
        servicoCalorico.salvarRegistro(obterUsuario(authentication), data, form.ingeridas, form.gastas);
        return "redirect:/perfil";
    }

    private void prepararModelo(Authentication authentication, Model model, LocalDate data) {
        ContaUsuario usuario = obterUsuario(authentication);
        PerfilFitness perfil = servicoCalorico.obterPerfil(usuario);
        RegistroCalorico registro = servicoCalorico.obterRegistro(usuario, data);
        model.addAttribute("perfil", perfil);
        model.addAttribute("registro", registro);
        model.addAttribute("data", data);
        model.addAttribute("perfilForm", perfil == null ? new PerfilForm() : new PerfilForm(perfil));
        model.addAttribute("registroForm", new RegistroForm());
        model.addAttribute("sexos", Sexo.values());
        model.addAttribute("atividades", NivelAtividade.values());
        if (perfil != null) {
            model.addAttribute("mifflin", Math.round(servicoCalorico.metabolismoMifflin(perfil)));
            model.addAttribute("harris", Math.round(servicoCalorico.metabolismoHarris(perfil)));
            model.addAttribute("medio", Math.round(servicoCalorico.metabolismoMedio(perfil)));
            model.addAttribute("total", Math.round(servicoCalorico.gastoTotalDiario(perfil)));
            model.addAttribute("meta", Math.round(servicoCalorico.metaCalorica(perfil)));
            model.addAttribute("situacao", servicoCalorico.situacao(perfil, registro));
        }
    }

    private ContaUsuario obterUsuario(Authentication authentication) {
        return repositorioContaUsuario.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário logado não encontrado"));
    }

    public static class PerfilForm {
        @Min(13) @Max(120) private int idade;
        @NotNull private Double altura;
        @NotNull private Double pesoAtual;
        @NotNull private Double pesoMeta;
        @NotNull private Sexo sexo;
        @NotNull private NivelAtividade nivelAtividade;
        public PerfilForm() {
        }
        public PerfilForm(PerfilFitness perfil) {
            idade = perfil.getIdade();
            altura = perfil.getAltura();
            pesoAtual = perfil.getPesoAtual();
            pesoMeta = perfil.getPesoMeta();
            sexo = perfil.getSexo();
            nivelAtividade = perfil.getNivelAtividade();
        }
        public int getIdade() { return idade; }
        public void setIdade(int idade) { this.idade = idade; }
        public Double getAltura() { return altura; }
        public void setAltura(Double altura) { this.altura = altura; }
        public Double getPesoAtual() { return pesoAtual; }
        public void setPesoAtual(Double pesoAtual) { this.pesoAtual = pesoAtual; }
        public Double getPesoMeta() { return pesoMeta; }
        public void setPesoMeta(Double pesoMeta) { this.pesoMeta = pesoMeta; }
        public Sexo getSexo() { return sexo; }
        public void setSexo(Sexo sexo) { this.sexo = sexo; }
        public NivelAtividade getNivelAtividade() { return nivelAtividade; }
        public void setNivelAtividade(NivelAtividade nivelAtividade) { this.nivelAtividade = nivelAtividade; }
    }

    public static class RegistroForm {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate data;
        @Min(0) private int ingeridas;
        @Min(0) private int gastas;
        public LocalDate getData() { return data; }
        public void setData(LocalDate data) { this.data = data; }
        public int getIngeridas() { return ingeridas; }
        public void setIngeridas(int ingeridas) { this.ingeridas = ingeridas; }
        public int getGastas() { return gastas; }
        public void setGastas(int gastas) { this.gastas = gastas; }
    }
}
