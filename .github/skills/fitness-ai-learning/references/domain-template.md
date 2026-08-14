# Domain Design Template

## Overview

For Phase 2 (Measurements & Calculations), this template shows how to design domain entities and services following Domain-Driven Design principles.

## Domain Model: Measurements

### Entity: Medicao (Measurement)

```java
@Entity
@Table(name = "medicoes")
public class Medicao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    // Core measurements
    @NotNull
    private Double peso; // kg
    
    @NotNull
    private Double altura; // m (stored once per user, rarely changes)
    
    @NotNull
    private Double cintura; // cm
    
    @NotNull
    private Double peito; // cm
    
    @NotNull
    private Double quadril; // cm
    
    @NotNull
    private Double braco; // cm
    
    @NotNull
    private Double perna; // cm
    
    // Calculated fields (computed from above)
    private Double imc; // BMI = peso / altura²
    private Double percentualGordura; // % body fat
    private Double percentualMassa; // % lean mass
    
    @CreationTimestamp
    private LocalDateTime dataMedicao;
    
    @CreationTimestamp
    private LocalDateTime dataHoraCriacao;
    
    public Medicao() {}
    
    public Medicao(Usuario usuario, Double peso, Double altura,
                   Double cintura, Double peito, Double quadril,
                   Double braco, Double perna) {
        this.usuario = usuario;
        this.peso = peso;
        this.altura = altura;
        this.cintura = cintura;
        this.peito = peito;
        this.quadril = quadril;
        this.braco = braco;
        this.perna = perna;
        this.dataMedicao = LocalDateTime.now();
        this.dataHoraCriacao = LocalDateTime.now();
    }
    
    // Getters & Setters
    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Double getPeso() { return peso; }
    public Double getAltura() { return altura; }
    // ... more getters
    
    @Override
    public String toString() {
        return "Medicao{" + "id=" + id + ", peso=" + peso + ", altura=" + altura
            + ", imc=" + imc + ", dataMedicao=" + dataMedicao + '}';
    }
}
```

### Value Object: CalculosFitness (Immutable)

```java
public class CalculosFitness {
    
    private final Double imc;
    private final Double percentualGordura;
    private final Double percentualMassa;
    
    public CalculosFitness(Double imc, Double percentualGordura, Double percentualMassa) {
        this.imc = Objects.requireNonNull(imc);
        this.percentualGordura = Objects.requireNonNull(percentualGordura);
        this.percentualMassa = Objects.requireNonNull(percentualMassa);
    }
    
    public Double getImc() { return imc; }
    public Double getPercentualGordura() { return percentualGordura; }
    public Double getPercentualMassa() { return percentualMassa; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CalculosFitness)) return false;
        CalculosFitness that = (CalculosFitness) o;
        return Objects.equals(imc, that.imc) &&
               Objects.equals(percentualGordura, that.percentualGordura) &&
               Objects.equals(percentualMassa, that.percentualMassa);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(imc, percentualGordura, percentualMassa);
    }
}
```

## Service Layer

### Service: ServicoCalculoMedicao

```java
@Service
@Transactional
public class ServicoCalculoMedicao {
    
    private static final Logger logger = LoggerFactory.getLogger(ServicoCalculoMedicao.class);
    
    private final RepositorioMedicao repositorio;
    
    public ServicoCalculoMedicao(RepositorioMedicao repositorio) {
        this.repositorio = repositorio;
    }
    
    // Calculate BMI
    public Double calcularIMC(Double peso, Double altura) {
        validarParametrosCalculoIMC(peso, altura);
        Double imc = peso / (altura * altura);
        logger.debug("BMI calculado: {} para peso {} e altura {}", imc, peso, altura);
        return imc;
    }
    
    // Calculate body fat percentage (usando fórmula Jackson-Pollack)
    public Double calcularPercentualGordura(Double idadeAnos, Double cintura, Double pesoPessoas) {
        // Fórmula Jackson-Pollack para homens (adaptar conforme necessário)
        // BodyFat% = 495 / (1.0324 - 0.19077 * log10(cintura - pescoço) + 0.15456 * log10(altura)) - 450
        // Aqui simplificado para demonstração
        Double densidadeCorpo = 1.0;
        if (idadeAnos > 0 && cintura > 0) {
            densidadeCorpo = 1.10938 - (0.0008987 * cintura) + (0.0000016 * cintura * cintura);
        }
        return (495 / densidadeCorpo) - 450;
    }
    
    // Calculate lean mass
    public Double calcularPercentualMassa(Double imc, Double peso) {
        return 100 - calcularPercentualGordura(25.0, 80.0, peso); // Simplificado
    }
    
    // Registrar nova medição
    public Medicao registrarMedicao(Usuario usuario, RegistroMedicaoDTO dto) {
        validarMedicaoDTO(dto);
        
        // Criar medição
        Medicao medicao = new Medicao(
            usuario,
            dto.getPeso(),
            dto.getAltura(),
            dto.getCintura(),
            dto.getPeito(),
            dto.getQuadril(),
            dto.getBraco(),
            dto.getPerna()
        );
        
        // Calcular valores derivados
        Double imc = calcularIMC(dto.getPeso(), dto.getAltura());
        Double percentualGordura = calcularPercentualGordura(usuario.getIdadeAnos(), dto.getCintura(), dto.getPeso());
        Double percentualMassa = calcularPercentualMassa(imc, dto.getPeso());
        
        medicao.setImc(imc);
        medicao.setPercentualGordura(percentualGordura);
        medicao.setPercentualMassa(percentualMassa);
        
        // Salvar
        Medicao salva = repositorio.save(medicao);
        logger.info("Medição registrada para usuário {}: IMC={}", usuario.getId(), imc);
        
        return salva;
    }
    
    // Obter última medição
    public Optional<Medicao> obterUltimaMedicao(Long usuarioId) {
        return repositorio.findFirstByUsuarioIdOrderByDataMedicaoDesc(usuarioId);
    }
    
    // Calcular progresso
    public ProgressoMedicao calcularProgresso(Long usuarioId) {
        List<Medicao> medicoes = repositorio.findByUsuarioIdOrderByDataMedicaoAsc(usuarioId);
        
        if (medicoes.isEmpty()) {
            return new ProgressoMedicao();
        }
        
        Medicao primeira = medicoes.get(0);
        Medicao ultima = medicoes.get(medicoes.size() - 1);
        
        return new ProgressoMedicao(
            primeira,
            ultima,
            ultima.getPeso() - primeira.getPeso(),
            ultima.getImc() - primeira.getImc()
        );
    }
    
    // Validations
    private void validarParametrosCalculoIMC(Double peso, Double altura) {
        if (peso == null || peso <= 0) {
            throw new IllegalArgumentException("Peso deve ser maior que zero");
        }
        if (altura == null || altura <= 0 || altura > 3) {
            throw new IllegalArgumentException("Altura inválida (deve estar entre 0.5m e 3m)");
        }
    }
    
    private void validarMedicaoDTO(RegistroMedicaoDTO dto) {
        if (dto.getPeso() == null || dto.getPeso() <= 30 || dto.getPeso() > 300) {
            throw new IllegalArgumentException("Peso deve estar entre 30kg e 300kg");
        }
        if (dto.getAltura() == null || dto.getAltura() < 0.5 || dto.getAltura() > 3.0) {
            throw new IllegalArgumentException("Altura deve estar entre 0.5m e 3.0m");
        }
        // ... mais validações
    }
}
```

## Repository

```java
@Repository
public interface RepositorioMedicao extends JpaRepository<Medicao, Long> {
    
    List<Medicao> findByUsuarioIdOrderByDataMedicaoAsc(Long usuarioId);
    
    List<Medicao> findByUsuarioIdAndDataMedicaoBetweenOrderByDataMedicaoAsc(
        Long usuarioId, LocalDateTime dataInicio, LocalDateTime dataFim);
    
    Optional<Medicao> findFirstByUsuarioIdOrderByDataMedicaoDesc(Long usuarioId);
    
    @Query("SELECT m FROM Medicao m WHERE m.usuario.id = ?1 ORDER BY m.dataMedicao DESC LIMIT 10")
    List<Medicao> findUltimasMedicoes(Long usuarioId);
}
```

## DTOs

### Input DTO: RegistroMedicaoDTO

```java
public class RegistroMedicaoDTO {
    
    @NotNull(message = "Peso é obrigatório")
    @DecimalMin("30")
    @DecimalMax("300")
    private Double peso;
    
    @NotNull(message = "Altura é obrigatória")
    @DecimalMin("0.5")
    @DecimalMax("3.0")
    private Double altura;
    
    @NotNull(message = "Cintura é obrigatória")
    @DecimalMin("50")
    private Double cintura;
    
    @NotNull(message = "Peito é obrigatório")
    @DecimalMin("50")
    private Double peito;
    
    @NotNull(message = "Quadril é obrigatório")
    @DecimalMin("50")
    private Double quadril;
    
    @NotNull(message = "Braço é obrigatório")
    @DecimalMin("15")
    @DecimalMax("45")
    private Double braco;
    
    @NotNull(message = "Perna é obrigatória")
    @DecimalMin("20")
    @DecimalMax("60")
    private Double perna;
    
    // Getters & Setters
}
```

### Output DTO: MedicaoDTO

```java
public class MedicaoDTO {
    
    private Long id;
    private Double peso;
    private Double altura;
    private Double imc;
    private Double percentualGordura;
    private Double percentualMassa;
    private LocalDateTime dataMedicao;
    
    public MedicaoDTO(Medicao medicao) {
        this.id = medicao.getId();
        this.peso = medicao.getPeso();
        this.altura = medicao.getAltura();
        this.imc = medicao.getImc();
        this.percentualGordura = medicao.getPercentualGordura();
        this.percentualMassa = medicao.getPercentualMassa();
        this.dataMedicao = medicao.getDataMedicao();
    }
    
    // Getters
}
```

## Controller: ControladorMedicoes

```java
@RestController
@RequestMapping("/api/medicoes")
public class ControladorMedicoes {
    
    private final ServicoCalculoMedicao servico;
    
    public ControladorMedicoes(ServicoCalculoMedicao servico) {
        this.servico = servico;
    }
    
    @PostMapping
    public ResponseEntity<MedicaoDTO> registrar(
            @Valid @RequestBody RegistroMedicaoDTO dto,
            Authentication auth) {
        Usuario usuario = obterUsuarioAutenticado(auth);
        Medicao medicao = servico.registrarMedicao(usuario, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new MedicaoDTO(medicao));
    }
    
    @GetMapping
    public ResponseEntity<List<MedicaoDTO>> listar(Authentication auth) {
        Usuario usuario = obterUsuarioAutenticado(auth);
        List<Medicao> medicoes = servico.obterMedicoes(usuario.getId());
        return ResponseEntity.ok(medicoes.stream()
            .map(MedicaoDTO::new)
            .collect(Collectors.toList()));
    }
    
    @GetMapping("/progresso")
    public ResponseEntity<ProgressoMedicaoDTO> obterProgresso(Authentication auth) {
        Usuario usuario = obterUsuarioAutenticado(auth);
        ProgressoMedicao progresso = servico.calcularProgresso(usuario.getId());
        return ResponseEntity.ok(new ProgressoMedicaoDTO(progresso));
    }
    
    private Usuario obterUsuarioAutenticado(Authentication auth) {
        // Get current user from auth
        return usuarioService.obterPorEmail(auth.getName());
    }
}
```

## Testing Measurements

See [Test Strategy](./test-strategy.md) for testing examples.

Quick test template:

```java
@ExtendWith(MockitoExtension.class)
class ServicoCalculoMedicaoTest {
    
    @Mock
    private RepositorioMedicao repositorio;
    
    @InjectMocks
    private ServicoCalculoMedicao servico;
    
    @Test
    void testCalculoIMCValido() {
        Double imc = servico.calcularIMC(70.0, 1.75);
        assertThat(imc).isCloseTo(22.86, Offset.offset(0.01));
    }
    
    @Test
    void testCalculoIMCComAlturaNula() {
        assertThatThrownBy(() -> servico.calcularIMC(70.0, null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```

## Questions to Ask AI

- "Generate the domain model for measurements with calculations"
- "Create DTOs and controller for measurements API"
- "Show me formulas for calculating body fat percentage"
- "Generate unit tests for measurement calculations"
