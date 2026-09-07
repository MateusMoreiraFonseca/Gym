package com.app.gym.infrastructure;

import com.app.gym.application.RepositorioContaUsuario;
import com.app.gym.application.ServicoMedicao;
import com.app.gym.domain.ContaUsuario;
import com.app.gym.domain.Medicao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medicoes")
public class ControladorMedicoesApi {
    private final RepositorioContaUsuario repositorioContaUsuario;
    private final ServicoMedicao servicoMedicao;

    public ControladorMedicoesApi(RepositorioContaUsuario repositorioContaUsuario, ServicoMedicao servicoMedicao) {
        this.repositorioContaUsuario = repositorioContaUsuario;
        this.servicoMedicao = servicoMedicao;
    }

    @GetMapping
    public List<RespostaMedicao> listar(Authentication authentication) {
        return servicoMedicao.listar(obterUsuario(authentication)).stream()
                .map(this::mapear)
                .toList();
    }

    @PostMapping
    public ResponseEntity<RespostaMedicao> registrar(
            Authentication authentication,
            @Valid @RequestBody RequisicaoMedicao requisicao) {
        Medicao medicao = servicoMedicao.registrar(
                obterUsuario(authentication), requisicao.peso(), requisicao.altura());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapear(medicao));
    }

    private ContaUsuario obterUsuario(Authentication authentication) {
        return repositorioContaUsuario.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário logado não encontrado"));
    }

    private RespostaMedicao mapear(Medicao medicao) {
        return new RespostaMedicao(
                medicao.getId(),
                medicao.getPeso(),
                medicao.getAltura(),
                medicao.getImc(),
                servicoMedicao.classificacaoImc(medicao.getImc()),
                medicao.getDataMedicao());
    }

    public record RequisicaoMedicao(
            @NotNull @DecimalMin("0.1") @DecimalMax("500") Double peso,
            @NotNull @DecimalMin("0.5") @DecimalMax("3") Double altura) {
    }

    public record RespostaMedicao(
            Long id,
            double peso,
            double altura,
            double imc,
            String classificacao,
            java.time.LocalDateTime dataMedicao) {
    }
}
