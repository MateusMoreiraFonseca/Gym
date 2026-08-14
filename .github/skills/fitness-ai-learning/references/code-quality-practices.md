# Code Quality & Best Practices

## Principles

### 1. Clear Naming
```java
// ❌ Bad
public String calc(double p, double a) {
    return String.format("%.2f", p / (a * a));
}

// ✅ Good
public String calculateBMI(double weightKg, double heightM) {
    return String.format("%.2f", weightKg / (heightM * heightM));
}
```

### 2. Single Responsibility Principle
Each class should have **one reason to change**.

```java
// ❌ Bad: Controller doing everything
@RestController
public class UserController {
    public void register(User user) {
        // validation
        // encryption
        // database save
        // email sending
    }
}

// ✅ Good: Separated concerns
@Service
public class ServicoRegistroUsuario {
    public Usuario registrar(RegistroDTO dto) {
        // ... registration logic
    }
}

@Service
public class ServicoEnvioEmail {
    public void enviarConfirmacao(Usuario usuario) {
        // ... email logic
    }
}
```

### 3. Don't Repeat Yourself (DRY)
Extract common patterns into shared methods.

```java
// ❌ Bad: Validation repeated
if (usuario.getNome() == null || usuario.getNome().isEmpty()) {
    throw new IllegalArgumentException("Nome não pode ser vazio");
}
if (usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
    throw new IllegalArgumentException("Email não pode ser vazio");
}

// ✅ Good: Extracted validation
private void validarCampoNaoVazio(String valor, String nomeCampo) {
    if (valor == null || valor.trim().isEmpty()) {
        throw new IllegalArgumentException(nomeCampo + " não pode ser vazio");
    }
}
```

### 4. Use Enums for Fixed Values
```java
// ❌ Bad
public class Usuario {
    private String tipoUsuario; // "admin", "user", "guest"
}

// ✅ Good
public enum TipoUsuario {
    ADMIN, USER, GUEST
}

public class Usuario {
    private TipoUsuario tipo;
}
```

### 5. Dependency Injection
```java
// ❌ Bad: Hard dependency
@Service
public class ServicoUsuario {
    private ServicoEmail email = new ServicoEmail();
}

// ✅ Good: Injected dependency
@Service
public class ServicoUsuario {
    private final ServicoEmail email;
    
    public ServicoUsuario(ServicoEmail email) {
        this.email = email;
    }
}
```

### 6. Error Handling
```java
// ❌ Bad: Generic exceptions
catch (Exception e) {
    throw new RuntimeException(e);
}

// ✅ Good: Specific exceptions
catch (DataIntegrityViolationException e) {
    throw new UsuarioJaExistenteException("Email já cadastrado", e);
}
```

### 7. Immutability Where Possible
```java
// ✅ Good: Final fields, no setters for immutable objects
public class Medida {
    private final LocalDateTime dataHora;
    private final Double peso;
    private final Double altura;
    
    public Medida(LocalDateTime dataHora, Double peso, Double altura) {
        this.dataHora = dataHora;
        this.peso = peso;
        this.altura = altura;
    }
}
```

### 8. Constants Over Magic Numbers
```java
// ❌ Bad
if (senha.length() < 8) {
    throw new IllegalArgumentException("Senha curta");
}

// ✅ Good
private static final int TAMANHO_MINIMO_SENHA = 8;

if (senha.length() < TAMANHO_MINIMO_SENHA) {
    throw new IllegalArgumentException("Senha deve ter no mínimo 8 caracteres");
}
```

## Code Organization

### File Structure
```
src/main/java/com/app/gym/
├── application/          # Services, use cases
├── domain/              # Entities, value objects, domain logic
├── infrastructure/      # Controllers, persistence, external integrations
└── config/              # Spring configuration
```

### Service Layer Pattern
```java
@Service
public class ServiçoExemplo {
    
    private final RepositorioExemplo repositorio;
    
    public ServiçoExemplo(RepositorioExemplo repositorio) {
        this.repositorio = repositorio;
    }
    
    public ResultadoDTO executarOperacao(EntradaDTO entrada) {
        // 1. Validar entrada
        validarEntrada(entrada);
        
        // 2. Executar lógica de negócio
        Resultado resultado = processarNegocio(entrada);
        
        // 3. Persistir se necessário
        repositorio.salvar(resultado);
        
        // 4. Retornar resultado
        return mapearParaDTO(resultado);
    }
    
    private void validarEntrada(EntradaDTO entrada) {
        // validation logic
    }
}
```

### Controller Pattern
```java
@RestController
@RequestMapping("/api/usuarios")
public class ControladorUsuarios {
    
    private final ServiçoUsuario servico;
    
    public ControladorUsuarios(ServiçoUsuario servico) {
        this.servico = servico;
    }
    
    @PostMapping
    public ResponseEntity<UsuarioDTO> criar(@Valid @RequestBody CriarUsuarioDTO dto) {
        try {
            Usuario usuario = servico.criar(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapearParaDTO(usuario));
        } catch (UsuarioJaExistenteException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obter(@PathVariable Long id) {
        Usuario usuario = servico.obterPorId(id);
        return ResponseEntity.ok(mapearParaDTO(usuario));
    }
}
```

## Logging Best Practices

```java
@Service
public class ServiçoExemplo {
    private static final Logger logger = LoggerFactory.getLogger(ServiçoExemplo.class);
    
    public void processar(String id) {
        logger.info("Iniciando processamento para ID: {}", id);
        
        try {
            // ... logic
            logger.debug("Processamento parcial concluído");
        } catch (Exception e) {
            logger.error("Erro ao processar ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Falha no processamento", e);
        }
    }
}
```

## Questions to Ask AI

- "Refactor this code to follow Single Responsibility Principle"
- "Extract common validation logic from these methods"
- "Review my service layer for best practices"
- "Suggest improvements for this controller method"
- "Show me idiomatic Java patterns for this scenario"

## Checklist Before Commit

- [ ] No hardcoded values (use constants)
- [ ] Clear, descriptive naming
- [ ] Single responsibility per class
- [ ] Proper error handling
- [ ] Logging at appropriate levels
- [ ] No unused imports
- [ ] Consistent formatting
