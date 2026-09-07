package com.app.gym.infrastructure;

import com.app.gym.application.RepositorioContaUsuario;
import com.app.gym.application.ServicoMedicao;
import com.app.gym.application.ServicoTreino;
import com.app.gym.application.ServicoCalorico;
import com.app.gym.domain.ContaUsuario;
import com.app.gym.domain.Medicao;
import com.app.gym.domain.TipoRecorrenciaTreino;
import com.app.gym.domain.Treino;
import com.app.gym.domain.RegistroTreino;
import com.app.gym.domain.IntensidadeTreino;
import com.app.gym.domain.RegistroCalorico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Controller
public class ControladorHome {
    private static final Locale LOCALE = Locale.forLanguageTag("pt-BR");
    private final RepositorioContaUsuario repositorioContaUsuario;
    private final ServicoMedicao servicoMedicao;
    private final ServicoTreino servicoTreino;
    private final ServicoCalorico servicoCalorico;

    public ControladorHome(RepositorioContaUsuario repositorioContaUsuario,
                           ServicoMedicao servicoMedicao, ServicoTreino servicoTreino) {
        this(repositorioContaUsuario, servicoMedicao, servicoTreino, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public ControladorHome(RepositorioContaUsuario repositorioContaUsuario,
                           ServicoMedicao servicoMedicao, ServicoTreino servicoTreino,
                           ServicoCalorico servicoCalorico) {
        this.repositorioContaUsuario = repositorioContaUsuario;
        this.servicoMedicao = servicoMedicao;
        this.servicoTreino = servicoTreino;
        this.servicoCalorico = servicoCalorico;
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String paginaInicial(Authentication authentication,
                                @RequestParam(required = false) Integer year,
                                @RequestParam(required = false) Integer month,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                Model model) {
        ContaUsuario usuario = obterUsuario(authentication);
        model.addAttribute("usuario", usuario);
        LocalDate selecionada = date == null ? LocalDate.now() : date;
        YearMonth calendario = year == null || month == null
                ? YearMonth.from(selecionada)
                : YearMonth.of(year, month);

        List<Medicao> medicoes = servicoMedicao.listar(usuario);
        model.addAttribute("mesAtual", calendario);
        model.addAttribute("mesAnterior", calendario.minusMonths(1));
        model.addAttribute("mesProximo", calendario.plusMonths(1));
        model.addAttribute("nomeMes", calendario.getMonth().getDisplayName(TextStyle.FULL, LOCALE));
        model.addAttribute("dias", construirDias(calendario, usuario, medicoes));
        model.addAttribute("dataSelecionada", selecionada);
        if (servicoCalorico != null) {
            model.addAttribute("registroCalorico", servicoCalorico.obterRegistro(usuario, selecionada));
        }
        model.addAttribute("medicoesDoDia", medicoes.stream()
                .filter(m -> m.getDataMedicao().toLocalDate().equals(selecionada)).toList());
        model.addAttribute("treinosDoDia", servicoTreino.listarDoDia(usuario, selecionada).stream()
                .map(treino -> new TreinoDoDia(treino, servicoTreino.obterRegistro(treino, selecionada)))
                .toList());
        model.addAttribute("treinoForm", new FormularioTreino());
        model.addAttribute("statusForm", new FormularioStatusTreino());
        return "home";
    }

    @PostMapping("/treinos")
    public String cadastrarTreino(Authentication authentication,
                                 @Valid @ModelAttribute("treinoForm") FormularioTreino form,
                                 BindingResult resultado, Model model) {
        LocalDate data = form.getDataSelecionada() == null ? LocalDate.now() : form.getDataSelecionada();
        if (resultado.hasErrors()) {
            return paginaInicial(authentication, data.getYear(), data.getMonthValue(), data, model);
        }
        try {
            servicoTreino.cadastrar(obterUsuario(authentication), form.getNome(), form.getDescricao(),
                    form.getTipoRecorrencia(), form.getDiasDaSemana(), form.getDataUnica(), data);
            return "redirect:/home?date=" + data;
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            return paginaInicial(authentication, data.getYear(), data.getMonthValue(), data, model);
        }
    }

    @PostMapping("/treinos/{id}/status")
    public String atualizarStatus(Authentication authentication,
                                  @org.springframework.web.bind.annotation.PathVariable Long id,
                                  @Valid @ModelAttribute("statusForm") FormularioStatusTreino form,
                                  BindingResult resultado, Model model) {
        LocalDate data = form.getData() == null ? LocalDate.now() : form.getData();
        if (!resultado.hasErrors()) {
            servicoTreino.listarDoDia(obterUsuario(authentication), data).stream()
                    .filter(treino -> treino.getId().equals(id))
                    .findFirst()
                    .ifPresent(treino -> servicoTreino.atualizarStatus(obterUsuario(authentication), treino, data,
                            form.isConcluido(), form.getTotalRepeticoes(), form.getDuracaoMinutos(), form.getIntensidade()));
        }
        return "redirect:/home?date=" + data;
    }

    private List<DiaCalendario> construirDias(YearMonth mes, ContaUsuario usuario, List<Medicao> medicoes) {
        List<DiaCalendario> dias = new ArrayList<>();
        LocalDate primeiro = mes.atDay(1);
        int deslocamento = primeiro.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < deslocamento; i++) {
            dias.add(DiaCalendario.criarVazio());
        }
        for (int dia = 1; dia <= mes.lengthOfMonth(); dia++) {
            LocalDate data = mes.atDay(dia);
            boolean temMedicao = medicoes.stream().anyMatch(m -> m.getDataMedicao().toLocalDate().equals(data));
            boolean temTreino = !servicoTreino.listarDoDia(usuario, data).isEmpty();
            dias.add(new DiaCalendario(data, false, temMedicao, temTreino, data.equals(LocalDate.now())));
        }
        return dias;
    }

    private ContaUsuario obterUsuario(Authentication authentication) {
        return repositorioContaUsuario.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário logado não encontrado"));
    }

    public record DiaCalendario(LocalDate data, boolean vazio, boolean temMedicao, boolean temTreino, boolean hoje) {
        public static DiaCalendario criarVazio() { return new DiaCalendario(null, true, false, false, false); }
    }

    public record TreinoDoDia(Treino treino, RegistroTreino registro) {
        public boolean concluido() { return registro != null && registro.isConcluido(); }
        public int kcalGastas() { return registro == null ? 0 : registro.getKcalGastas(); }
        public int totalRepeticoes() { return registro == null ? 0 : registro.getTotalRepeticoes(); }
    }

    public static class FormularioTreino {
        @NotBlank
        private String nome;
        private String descricao;
        private TipoRecorrenciaTreino tipoRecorrencia = TipoRecorrenciaTreino.DIA_DA_SEMANA;
        private Set<DayOfWeek> diasDaSemana = new java.util.HashSet<>();
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate dataUnica;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate dataSelecionada;

        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        public TipoRecorrenciaTreino getTipoRecorrencia() { return tipoRecorrencia; }
        public void setTipoRecorrencia(TipoRecorrenciaTreino tipoRecorrencia) { this.tipoRecorrencia = tipoRecorrencia; }
        public Set<DayOfWeek> getDiasDaSemana() { return diasDaSemana; }
        public void setDiasDaSemana(Set<DayOfWeek> diasDaSemana) { this.diasDaSemana = diasDaSemana; }
        public LocalDate getDataUnica() { return dataUnica; }
        public void setDataUnica(LocalDate dataUnica) { this.dataUnica = dataUnica; }
        public LocalDate getDataSelecionada() { return dataSelecionada; }
        public void setDataSelecionada(LocalDate dataSelecionada) { this.dataSelecionada = dataSelecionada; }
    }

    public static class FormularioStatusTreino {
        private LocalDate data;
        private boolean concluido;
        private int totalRepeticoes = 30;
        private int duracaoMinutos = 30;
        private IntensidadeTreino intensidade = IntensidadeTreino.MODERADA;
        public LocalDate getData() { return data; }
        public void setData(LocalDate data) { this.data = data; }
        public boolean isConcluido() { return concluido; }
        public void setConcluido(boolean concluido) { this.concluido = concluido; }
        public int getTotalRepeticoes() { return totalRepeticoes; }
        public void setTotalRepeticoes(int totalRepeticoes) { this.totalRepeticoes = totalRepeticoes; }
        public int getDuracaoMinutos() { return duracaoMinutos; }
        public void setDuracaoMinutos(int duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }
        public IntensidadeTreino getIntensidade() { return intensidade; }
        public void setIntensidade(IntensidadeTreino intensidade) { this.intensidade = intensidade; }
    }
}
