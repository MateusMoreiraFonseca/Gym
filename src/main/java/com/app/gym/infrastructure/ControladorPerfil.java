package com.app.gym.infrastructure;

import com.app.gym.application.RepositorioContaUsuario;
import com.app.gym.application.ServicoCriptografiaSenha;
import com.app.gym.application.ServicoCalorico;
import com.app.gym.domain.ContaUsuario;
import com.app.gym.domain.PerfilFitness;
import com.app.gym.domain.RegistroCalorico;
import com.app.gym.domain.Sexo;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Set;
import java.util.Objects;
import java.time.LocalDate;

@Controller
public class ControladorPerfil {

    private final RepositorioContaUsuario repositorioContaUsuario;
    private final com.app.gym.application.ServicoCriptografiaSenha servicoCriptografiaSenha;
    private final ServicoCalorico servicoCalorico;

    public ControladorPerfil(RepositorioContaUsuario repositorioContaUsuario, ServicoCriptografiaSenha servicoCriptografiaSenha) {
        this(repositorioContaUsuario, servicoCriptografiaSenha, null);
    }

    @Autowired
    public ControladorPerfil(RepositorioContaUsuario repositorioContaUsuario,
                             ServicoCriptografiaSenha servicoCriptografiaSenha,
                             ServicoCalorico servicoCalorico) {
        this.repositorioContaUsuario = repositorioContaUsuario;
        this.servicoCriptografiaSenha = servicoCriptografiaSenha;
        this.servicoCalorico = servicoCalorico;
    }

    @GetMapping("/perfil")
    public String paginaPerfil(Authentication authentication, Model model,
                               @RequestParam(required = false) String mensagem,
                               @RequestParam(defaultValue = "mes") String periodo) {
        ContaUsuario usuario = obterUsuario(authentication);
        model.addAttribute("usuario", usuario);
        if (servicoCalorico != null) {
            prepararDadosFitness(usuario, model, LocalDate.now(), periodo);
        }
        if ("foto-salva".equals(mensagem)) {
            model.addAttribute("successMessage", "Foto de perfil atualizada com sucesso.");
        }

        return "perfil";
    }

    private void prepararDadosFitness(ContaUsuario usuario, Model model, LocalDate data, String periodo) {
            PerfilFitness perfil = servicoCalorico.obterPerfil(usuario);
            RegistroCalorico registro = servicoCalorico.obterRegistro(usuario, data);
            model.addAttribute("perfil", perfil);
            model.addAttribute("registro", registro);
            model.addAttribute("data", data);
            model.addAttribute("perfilForm", perfil == null
                    ? new ControladorCalorias.PerfilForm()
                    : new ControladorCalorias.PerfilForm(perfil));
            model.addAttribute("registroForm", new ControladorCalorias.RegistroForm());
            model.addAttribute("sexos", Sexo.values());
            String periodoSelecionado = Set.of("mes", "semana", "todo").contains(periodo) ? periodo : "mes";
            model.addAttribute("periodo", periodoSelecionado);
            model.addAttribute("mediaCalorica", servicoCalorico.mediaCalorica(usuario, periodoSelecionado, data));
            model.addAttribute("historicoCalorico", servicoCalorico.historicoCalorico(usuario, periodoSelecionado, data));
            if (perfil != null) {
                model.addAttribute("mifflin", Math.round(servicoCalorico.metabolismoMifflin(perfil)));
                model.addAttribute("harris", Math.round(servicoCalorico.metabolismoHarris(perfil)));
                model.addAttribute("medio", Math.round(servicoCalorico.metabolismoMedio(perfil)));
                model.addAttribute("total", Math.round(servicoCalorico.gastoTotalDiario(perfil)));
                model.addAttribute("meta", Math.round(servicoCalorico.metaCalorica(perfil)));
                model.addAttribute("situacao", servicoCalorico.situacao(perfil, registro));
            }
    }

    @PostMapping("/perfil/foto")
    public String atualizarFotoPerfil(Authentication authentication,
                                     @RequestParam("foto") MultipartFile foto,
                                     Model model) {
        ContaUsuario usuario = obterUsuario(authentication);
        if (foto.isEmpty()) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("errorMessage", "Selecione uma imagem para enviar.");
            return "perfil";
        }
        if (foto.getSize() > 5 * 1024 * 1024) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("errorMessage", "A foto deve ter no máximo 5 MB.");
            return "perfil";
        }
        String tipoMime = foto.getContentType();
        if (tipoMime == null || !Set.of("image/jpeg", "image/png", "image/gif", "image/webp").contains(tipoMime.toLowerCase())) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("errorMessage", "Envie um arquivo de imagem válido.");
            return "perfil";
        }
        try {
            usuario.atualizarFotoPerfil(foto.getBytes(), tipoMime);
            repositorioContaUsuario.save(usuario);
            return "redirect:/perfil?mensagem=foto-salva";
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível ler a foto enviada.", exception);
        }
    }

    @GetMapping("/perfil/foto")
    @ResponseBody
    public ResponseEntity<byte[]> fotoPerfil(Authentication authentication) {
        ContaUsuario usuario = obterUsuario(authentication);
        if (!usuario.possuiFotoPerfil()) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.parseMediaType(usuario.getTipoMimeFotoPerfil());
        return ResponseEntity.ok().contentType(mediaType).body(usuario.getFotoPerfil());
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
        usuario.atualizarSenha(novaSenhaCriptografada);
        repositorioContaUsuario.save(usuario);

        model.addAttribute("successMessage", "Senha atualizada com sucesso.");
        return "editar-perfil";
    }

    private ContaUsuario obterUsuario(Authentication authentication) {
        return repositorioContaUsuario.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário logado não encontrado"));
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
