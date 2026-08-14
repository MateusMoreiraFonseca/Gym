# Graph Data Template

## Overview

For Phase 3 (Graphs & Visualization), this template shows how to structure data queries and APIs for chart visualization.

## Data Aggregation Service

### Service: ServicoAgregacaoMedicoes

```java
@Service
public class ServicoAgregacaoMedicoes {
    
    private final RepositorioMedicao repositorio;
    private final RepositorioMedicaoAgregada agregadoRepository;
    
    public ServicoAgregacaoMedicoes(RepositorioMedicao repositorio,
                                    RepositorioMedicaoAgregada agregadoRepository) {
        this.repositorio = repositorio;
        this.agregadoRepository = agregadoRepository;
    }
    
    // Get data for weight trend chart (Peso x Dias)
    public DadosGraficoTendencia obterTendenciaPeso(Long usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        List<Medicao> medicoes = repositorio.findByUsuarioIdAndDataMedicaoBetweenOrderByDataMedicaoAsc(
            usuarioId,
            dataInicio.atStartOfDay(),
            dataFim.atEndOfDay()
        );
        
        List<LocalDate> datas = new ArrayList<>();
        List<Double> pesos = new ArrayList<>();
        
        for (Medicao m : medicoes) {
            datas.add(m.getDataMedicao().toLocalDate());
            pesos.add(m.getPeso());
        }
        
        return new DadosGraficoTendencia(datas, pesos, "Peso (kg)");
    }
    
    // Get data for BMI trend chart
    public DadosGraficoTendencia obterTendenciaIMC(Long usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        List<Medicao> medicoes = repositorio.findByUsuarioIdAndDataMedicaoBetweenOrderByDataMedicaoAsc(
            usuarioId,
            dataInicio.atStartOfDay(),
            dataFim.atEndOfDay()
        );
        
        List<LocalDate> datas = new ArrayList<>();
        List<Double> imcs = new ArrayList<>();
        
        for (Medicao m : medicoes) {
            datas.add(m.getDataMedicao().toLocalDate());
            imcs.add(m.getImc());
        }
        
        return new DadosGraficoTendencia(datas, imcs, "IMC");
    }
    
    // Get data for body measurements chart (cintura, peito, quadril, etc.)
    public DadosGraficoMultiplasLinhas obterTendenciasMedidas(Long usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        List<Medicao> medicoes = repositorio.findByUsuarioIdAndDataMedicaoBetweenOrderByDataMedicaoAsc(
            usuarioId,
            dataInicio.atStartOfDay(),
            dataFim.atEndOfDay()
        );
        
        List<LocalDate> datas = new ArrayList<>();
        List<Double> cinturas = new ArrayList<>();
        List<Double> peitos = new ArrayList<>();
        List<Double> quadris = new ArrayList<>();
        List<Double> bracos = new ArrayList<>();
        List<Double> pernas = new ArrayList<>();
        
        for (Medicao m : medicoes) {
            datas.add(m.getDataMedicao().toLocalDate());
            cinturas.add(m.getCintura());
            peitos.add(m.getPeito());
            quadris.add(m.getQuadril());
            bracos.add(m.getBraco());
            pernas.add(m.getPerna());
        }
        
        return new DadosGraficoMultiplasLinhas(
            datas,
            Map.of(
                "Cintura", cinturas,
                "Peito", peitos,
                "Quadril", quadris,
                "Braço", bracos,
                "Perna", pernas
            )
        );
    }
    
    // Get summary statistics
    public EstatisticaMedicoes obterEstatisticas(Long usuarioId) {
        Optional<Medicao> ultima = repositorio.findFirstByUsuarioIdOrderByDataMedicaoDesc(usuarioId);
        
        if (ultima.isEmpty()) {
            return new EstatisticaMedicoes();
        }
        
        Medicao ultimaMedicao = ultima.get();
        
        List<Medicao> medicoes = repositorio.findByUsuarioIdOrderByDataMedicaoAsc(usuarioId);
        Medicao primeira = medicoes.get(0);
        
        return new EstatisticaMedicoes(
            ultimaMedicao.getPeso(),
            primeira.getPeso(),
            ultimaMedicao.getImc(),
            primeira.getImc(),
            ultimaMedicao.getPercentualGordura(),
            primeira.getPercentualGordura(),
            primeira.getDataMedicao().toLocalDate(),
            ultimaMedicao.getDataMedicao().toLocalDate()
        );
    }
    
    // Aggregate data by period (Daily, Weekly, Monthly)
    public List<MedicaoAgregada> agregaPorPeriodo(Long usuarioId, TipoPeriodo periodo) {
        List<Medicao> medicoes = repositorio.findByUsuarioIdOrderByDataMedicaoAsc(usuarioId);
        
        Map<YearMonth, List<Medicao>> agrupado = medicoes.stream()
            .collect(Collectors.groupingBy(m -> YearMonth.from(m.getDataMedicao())));
        
        List<MedicaoAgregada> resultado = new ArrayList<>();
        
        for (Map.Entry<YearMonth, List<Medicao>> entry : agrupado.entrySet()) {
            List<Medicao> grupo = entry.getValue();
            
            Double pesomedio = grupo.stream()
                .mapToDouble(Medicao::getPeso)
                .average()
                .orElse(0.0);
            
            Double imcMedio = grupo.stream()
                .mapToDouble(Medicao::getImc)
                .average()
                .orElse(0.0);
            
            resultado.add(new MedicaoAgregada(
                entry.getKey().atDay(1),
                pesomedio,
                imcMedio,
                grupo.size()
            ));
        }
        
        return resultado;
    }
}
```

## DTOs para Gráficos

### SimpleChart DTO

```java
public class DadosGraficoTendencia {
    
    private List<String> labels; // Datas em formato "DD/MM"
    private List<Double> valores;
    private String nomeEixoY;
    
    public DadosGraficoTendencia(List<LocalDate> datas, List<Double> valores, String nomeEixoY) {
        this.labels = datas.stream()
            .map(d -> d.format(DateTimeFormatter.ofPattern("dd/MM")))
            .collect(Collectors.toList());
        this.valores = valores;
        this.nomeEixoY = nomeEixoY;
    }
    
    public List<String> getLabels() { return labels; }
    public List<Double> getValores() { return valores; }
    public String getNomeEixoY() { return nomeEixoY; }
}
```

### Multi-line Chart DTO

```java
public class DadosGraficoMultiplasLinhas {
    
    private List<String> labels; // Datas
    private Map<String, List<Double>> datasets; // Nome da série -> valores
    
    public DadosGraficoMultiplasLinhas(List<LocalDate> datas, Map<String, List<Double>> datasets) {
        this.labels = datas.stream()
            .map(d -> d.format(DateTimeFormatter.ofPattern("dd/MM")))
            .collect(Collectors.toList());
        this.datasets = datasets;
    }
    
    public List<String> getLabels() { return labels; }
    public Map<String, List<Double>> getDatasets() { return datasets; }
}
```

### Statistics DTO

```java
public class EstatisticaMedicoes {
    
    private Double pesoAtual;
    private Double pesoPrimeira;
    private Double variacaoPeso;
    private Double percentualVariacao;
    
    private Double imcAtual;
    private Double imcPrimeira;
    private Double variacaoImc;
    
    private Double percentualGorduraAtual;
    private Double percentualGorduraPrimeira;
    
    private LocalDate dataPrimeiraM medicao;
    private LocalDate dataUltimaMedicao;
    private int totalMedicoes;
    
    // Getters
}
```

## API Endpoints

### ControladorGraficos

```java
@RestController
@RequestMapping("/api/graficos")
public class ControladorGraficos {
    
    private final ServicoAgregacaoMedicoes servico;
    
    public ControladorGraficos(ServicoAgregacaoMedicoes servico) {
        this.servico = servico;
    }
    
    // Peso x Dias
    @GetMapping("/peso")
    public ResponseEntity<DadosGraficoTendencia> obterGraficoPeso(
            @RequestParam(defaultValue = "-30") int diasAtrás,
            Authentication auth) {
        Usuario usuario = obterUsuarioAutenticado(auth);
        LocalDate dataFim = LocalDate.now();
        LocalDate dataInicio = dataFim.minusDays(Math.abs(diasAtrás));
        
        DadosGraficoTendencia dados = servico.obterTendenciaPeso(usuario.getId(), dataInicio, dataFim);
        return ResponseEntity.ok(dados);
    }
    
    // IMC x Dias
    @GetMapping("/imc")
    public ResponseEntity<DadosGraficoTendencia> obterGraficoIMC(
            @RequestParam(defaultValue = "-30") int diasAtrás,
            Authentication auth) {
        Usuario usuario = obterUsuarioAutenticado(auth);
        LocalDate dataFim = LocalDate.now();
        LocalDate dataInicio = dataFim.minusDays(Math.abs(diasAtrás));
        
        DadosGraficoTendencia dados = servico.obterTendenciaIMC(usuario.getId(), dataInicio, dataFim);
        return ResponseEntity.ok(dados);
    }
    
    // Medidas (cintura, peito, quadril, etc.)
    @GetMapping("/medidas")
    public ResponseEntity<DadosGraficoMultiplasLinhas> obterGraficoMedidas(
            @RequestParam(defaultValue = "-30") int diasAtrás,
            Authentication auth) {
        Usuario usuario = obterUsuarioAutenticado(auth);
        LocalDate dataFim = LocalDate.now();
        LocalDate dataInicio = dataFim.minusDays(Math.abs(diasAtrás));
        
        DadosGraficoMultiplasLinhas dados = servico.obterTendenciasMedidas(usuario.getId(), dataInicio, dataFim);
        return ResponseEntity.ok(dados);
    }
    
    // Estatísticas geral
    @GetMapping("/stats")
    public ResponseEntity<EstatisticaMedicoes> obterEstatisticas(Authentication auth) {
        Usuario usuario = obterUsuarioAutenticado(auth);
        EstatisticaMedicoes stats = servico.obterEstatisticas(usuario.getId());
        return ResponseEntity.ok(stats);
    }
}
```

## Frontend: HTML + Chart.js

### Template: dashboard.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Dashboard - Meu Progresso</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <div class="container">
        <h1>Meu Progresso</h1>
        
        <!-- Statistics -->
        <div id="stats" class="stats-container"></div>
        
        <!-- Weight Chart -->
        <div class="chart-container">
            <h2>Peso x Dias (últimos 30 dias)</h2>
            <canvas id="chartPeso"></canvas>
        </div>
        
        <!-- BMI Chart -->
        <div class="chart-container">
            <h2>IMC x Dias (últimos 30 dias)</h2>
            <canvas id="chartImc"></canvas>
        </div>
        
        <!-- Body Measurements Chart -->
        <div class="chart-container">
            <h2>Medidas (últimos 30 dias)</h2>
            <canvas id="chartMedidas"></canvas>
        </div>
    </div>
    
    <script>
        const API_BASE = '/api/graficos';
        
        // Load statistics
        fetch(`${API_BASE}/stats`)
            .then(r => r.json())
            .then(stats => {
                const container = document.getElementById('stats');
                container.innerHTML = `
                    <div class="stat-item">
                        <h3>Peso Atual: ${stats.pesoAtual} kg</h3>
                        <p>Variação: ${stats.variacaoPeso > 0 ? '+' : ''}${stats.variacaoPeso.toFixed(1)} kg (${stats.percentualVariacao}%)</p>
                    </div>
                    <div class="stat-item">
                        <h3>IMC Atual: ${stats.imcAtual.toFixed(1)}</h3>
                        <p>Variação: ${stats.variacaoImc.toFixed(1)}</p>
                    </div>
                    <div class="stat-item">
                        <h3>Total de Medições: ${stats.totalMedicoes}</h3>
                        <p>Acompanhando desde ${stats.dataPrimeiraM medicao}</p>
                    </div>
                `;
            });
        
        // Weight Chart
        fetch(`${API_BASE}/peso?diasAtrás=-30`)
            .then(r => r.json())
            .then(data => {
                const ctx = document.getElementById('chartPeso').getContext('2d');
                new Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: data.labels,
                        datasets: [{
                            label: data.nomeEixoY,
                            data: data.valores,
                            borderColor: 'rgb(75, 192, 192)',
                            backgroundColor: 'rgba(75, 192, 192, 0.1)',
                            tension: 0.4
                        }]
                    },
                    options: {
                        responsive: true,
                        scales: {
                            y: { beginAtZero: false }
                        }
                    }
                });
            });
        
        // BMI Chart
        fetch(`${API_BASE}/imc?diasAtrás=-30`)
            .then(r => r.json())
            .then(data => {
                const ctx = document.getElementById('chartImc').getContext('2d');
                new Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: data.labels,
                        datasets: [{
                            label: data.nomeEixoY,
                            data: data.valores,
                            borderColor: 'rgb(153, 102, 255)',
                            backgroundColor: 'rgba(153, 102, 255, 0.1)',
                            tension: 0.4
                        }]
                    },
                    options: {
                        responsive: true,
                        scales: {
                            y: { beginAtZero: false }
                        }
                    }
                });
            });
        
        // Body Measurements Chart
        fetch(`${API_BASE}/medidas?diasAtrás=-30`)
            .then(r => r.json())
            .then(data => {
                const ctx = document.getElementById('chartMedidas').getContext('2d');
                const colors = ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF'];
                const datasets = [];
                
                Object.entries(data.datasets).forEach((entry, idx) => {
                    datasets.push({
                        label: entry[0],
                        data: entry[1],
                        borderColor: colors[idx],
                        backgroundColor: colors[idx] + '20',
                        tension: 0.4
                    });
                });
                
                new Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: data.labels,
                        datasets: datasets
                    },
                    options: {
                        responsive: true,
                        scales: {
                            y: { beginAtZero: false }
                        }
                    }
                });
            });
    </script>
</body>
</html>
```

## Database Query Optimization

### Entity Query (avoid N+1)

```java
// ❌ Bad: N+1 queries
List<Medicao> medicoes = repositorio.findAll();
medicoes.forEach(m -> System.out.println(m.getUsuario().getNome())); // N queries!

// ✅ Good: Single query with JOIN FETCH
@Query("SELECT m FROM Medicao m JOIN FETCH m.usuario WHERE m.usuario.id = ?1")
List<Medicao> findWithUsuarioEager(Long usuarioId);
```

## Testing Charts

```java
@ExtendWith(MockitoExtension.class)
class ServicoAgregacaoMedicoesTest {
    
    @Mock
    private RepositorioMedicao repositorio;
    
    @InjectMocks
    private ServicoAgregacaoMedicoes servico;
    
    @Test
    void testObtenerTendenciaPeso() {
        // Given
        LocalDate data1 = LocalDate.now().minusDays(5);
        LocalDate data2 = LocalDate.now();
        List<Medicao> medicoes = List.of(
            new Medicao(..., 70.0, ...),
            new Medicao(..., 71.0, ...)
        );
        when(repositorio.findByUsuarioIdAndDataMedicaoBetweenOrderByDataMedicaoAsc(
            anyLong(), any(), any())).thenReturn(medicoes);
        
        // When
        DadosGraficoTendencia dados = servico.obterTendenciaPeso(1L, data1, data2);
        
        // Then
        assertThat(dados.getValores()).containsExactly(70.0, 71.0);
    }
}
```

## Questions to Ask AI

- "Generate API endpoints for chart data aggregation"
- "Create Chart.js visualizations for weight tracking"
- "Show me how to optimize database queries for dashboards"
- "Generate unit tests for data aggregation service"
- "Create responsive dashboard layout with multiple charts"
