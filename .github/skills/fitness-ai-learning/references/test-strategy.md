# Testing Strategy

## Overview

This document defines how to test the Gym Fitness application at different levels: unit, integration, and end-to-end.

## Test Pyramid

```
        / E2E Tests \           (Few, slow, comprehensive)
       /   Selenium   \
      /________________\

     / Integration \          (Some, medium speed)
    /     Tests     \
   /________________\

  /  Unit Tests  \             (Many, fast, focused)
 /________________\
```

### Test Distribution

- **Unit Tests**: 70% – Fast, focused on single components
- **Integration Tests**: 20% – Test components working together
- **E2E Tests**: 10% – Critical user flows

## Unit Tests

### Characteristics

- ✓ Fast (< 100ms each)
- ✓ Focused on single class/method
- ✓ No database, no HTTP calls
- ✓ Use mocks for dependencies
- ✓ No side effects

### Tools

- **Framework**: JUnit 5
- **Mocking**: Mockito
- **Assertions**: AssertJ or Hamcrest

### Examples

#### Test Password Encryption

```java
@Test
void testSenhaEhCriptografadaCorretamente() {
    // Given
    String senhaRaw = "minhaSenha123!";
    
    // When
    String senhaCriptografada = servico.criptografar(senhaRaw);
    
    // Then
    assertThat(senhaCriptografada).isNotEqualTo(senhaRaw);
    assertThat(servico.validar(senhaRaw, senhaCriptografada)).isTrue();
    assertThat(servico.validar("outraSenha", senhaCriptografada)).isFalse();
}
```

#### Test BMI Calculation

```java
@Test
void testCalculoBMI() {
    // Given
    double peso = 70.0;
    double altura = 1.75;
    
    // When
    double bmi = servico.calcularIMC(peso, altura);
    
    // Then
    assertThat(bmi).isCloseTo(22.86, Offset.offset(0.01));
}

@ParameterizedTest
@CsvSource({
    "50, 1.50, 22.22",
    "70, 1.75, 22.86",
    "90, 1.80, 27.78"
})
void testCalculoBMIVariosValores(double peso, double altura, double esperado) {
    double bmi = servico.calcularIMC(peso, altura);
    assertThat(bmi).isCloseTo(esperado, Offset.offset(0.01));
}
```

#### Test User Validation

```java
@Test
void testRegistroUsuarioComEmailInvalido() {
    // Given
    RegistroDTO dto = new RegistroDTO();
    dto.setEmail("emailInvalido");
    dto.setSenha("Senha123!");
    
    // When & Then
    assertThatThrownBy(() -> servico.registrar(dto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Email inválido");
}

@Test
void testRegistroUsuarioComSenhaFraca() {
    // Given
    RegistroDTO dto = new RegistroDTO();
    dto.setEmail("usuario@email.com");
    dto.setSenha("123");
    
    // When & Then
    assertThatThrownBy(() -> servico.registrar(dto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("senha");
}
```

#### Test with Mocks

```java
@ExtendWith(MockitoExtension.class)
class ServicoUsuarioTest {
    
    @Mock
    private RepositorioUsuario repositorio;
    
    @Mock
    private ServicoEmail email;
    
    @InjectMocks
    private ServicoUsuario servico;
    
    @Test
    void testRegistroUsuarioEnviaEmail() {
        // Given
        RegistroDTO dto = new RegistroDTO("novo@email.com", "Senha123!");
        Usuario usuario = new Usuario("novo@email.com");
        when(repositorio.salvar(any())).thenReturn(usuario);
        
        // When
        servico.registrar(dto);
        
        // Then
        verify(repositorio).salvar(any(Usuario.class));
        verify(email).enviarConfirmacao(usuario);
    }
}
```

## Integration Tests

### Characteristics

- ✓ Test multiple components together
- ✓ Use real database (H2 or TestContainers)
- ✓ Medium speed (100ms - 1s each)
- ✓ Test API contracts
- ✓ Test repository queries

### Tools

- **Spring Test**: `@SpringBootTest`
- **Database**: H2 for tests
- **API Testing**: `MockMvc` or `RestTemplate`
- **Containers**: TestContainers for real database

### Examples

#### Test API Endpoint

```java
@SpringBootTest
@AutoConfigureMockMvc
class ControladorAutenticacaoIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private RepositorioUsuario repositorio;
    
    @Test
    void testLoginComCredenciaisValidas() throws Exception {
        // Given
        Usuario usuario = new Usuario("teste@email.com", "SenhaHash123");
        repositorio.save(usuario);
        
        // When & Then
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"teste@email.com\", \"senha\": \"SenhaHash123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
    }
    
    @Test
    void testLoginComCredenciaisInvalidas() throws Exception {
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"inexistente@email.com\", \"senha\": \"123\"}"))
            .andExpect(status().isUnauthorized());
    }
}
```

#### Test Repository Query

```java
@DataJpaTest
class RepositorioUsuarioTest {
    
    @Autowired
    private RepositorioUsuario repositorio;
    
    @Test
    void testFindByEmail() {
        // Given
        Usuario usuario = new Usuario("teste@email.com");
        repositorio.save(usuario);
        
        // When
        Optional<Usuario> resultado = repositorio.findByEmail("teste@email.com");
        
        // Then
        assertThat(resultado).isPresent()
            .contains(usuario);
    }
}
```

#### Test with TestContainers

```java
@SpringBootTest
@Testcontainers
class MedicaoRepositoryTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("test")
        .withUsername("test")
        .withPassword("test");
    
    @Autowired
    private RepositorioMedicao repositorio;
    
    @Test
    void testSalvarMedicao() {
        // Given
        Medicao medicao = new Medicao(70.0, 1.75, LocalDateTime.now());
        
        // When
        Medicao salva = repositorio.save(medicao);
        
        // Then
        assertThat(salva.getId()).isNotNull();
    }
}
```

## Test Data Builders

Use builders to create test objects clearly:

```java
public class UsuarioTestBuilder {
    private String email = "padrao@email.com";
    private String senha = "SenhaHash123";
    private String nome = "Usuário Teste";
    
    public UsuarioTestBuilder comEmail(String email) {
        this.email = email;
        return this;
    }
    
    public Usuario build() {
        return new Usuario(email, senha, nome);
    }
}

// Usage
@Test
void teste() {
    Usuario usuario = new UsuarioTestBuilder()
        .comEmail("novo@email.com")
        .build();
}
```

## Test Coverage Goals

| Component | Target |
|-----------|--------|
| Entities | 90% |
| Services | 85% |
| Controllers | 75% |
| Repositories | 70% |
| Utilities | 95% |
| **Overall** | **80%** |

### Check Coverage

```bash
mvn test jacoco:report
# Report: target/site/jacoco/index.html
```

## E2E Tests (Optional for MVP)

For critical user flows only:

```java
@SpringBootTest
class UserJourneyE2ETest {
    
    @Test
    void testCompleteRegistrationAndLogin() {
        // 1. Register new user
        // 2. Verify email
        // 3. Login
        // 4. Create measurement
        // 5. View dashboard
    }
}
```

## Running Tests

```bash
# All tests
mvn test

# Single test class
mvn test -Dtest=ServicoUsuarioTest

# Single test method
mvn test -Dtest=ServicoUsuarioTest#testSenhaEhCriptografadaCorretamente

# With coverage
mvn test jacoco:report

# Fail fast
mvn test -DfailIfNoTests=false -ff
```

## Test Template (TDD)

```java
@ExtendWith(MockitoExtension.class)
class Seu ComponenteTest {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    // Dependencies
    @Mock
    private RepositorioExemplo repositorio;
    
    @InjectMocks
    private ServicoExemplo servico;
    
    @BeforeEach
    void setup() {
        // Setup common test data
    }
    
    // Happy path test
    @Test
    void testFuncionamentoNormal() {
        // Given
        
        // When
        
        // Then
    }
    
    // Edge case test
    @Test
    void testCasoLimite() {
        // Given
        
        // When
        
        // Then
    }
    
    // Error case test
    @Test
    void testComErro() {
        // Given
        
        // When & Then
        assertThatThrownBy(() -> {})
            .isInstanceOf(Exception.class);
    }
}
```

## Checklist Before Commit

- [ ] All tests passing locally
- [ ] New code has tests
- [ ] Coverage >80%
- [ ] No skipped tests
- [ ] No hardcoded test data (use builders)
- [ ] Tests are fast (<1s per test)
- [ ] Clear test names (what, when, expected)

## Questions to Ask AI

- "Generate unit tests for this service"
- "Create integration tests for this API endpoint"
- "What edge cases should I test for this calculation?"
- "Show me how to test authorization in this controller"
- "Generate test data builders for my entities"
