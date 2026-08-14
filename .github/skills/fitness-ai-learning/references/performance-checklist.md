# Performance & Optimization Checklist

## Objective

Ensure the Gym Fitness application performs well, scales properly, and provides a responsive user experience.

## Database Optimization

### 1. N+1 Query Problem

**Problem**: Loading a list of entities, then accessing a relationship for each entity results in N+1 queries.

```java
// ❌ Bad: 1 query for list + N queries for each usuario
List<Medicao> medicoes = repositorio.findAll(); // 1 query
medicoes.forEach(m -> {
    System.out.println(m.getUsuario().getNome()); // N additional queries!
});
```

**Solution**: Use JOIN FETCH or batch loading

```java
// ✅ Good: 1 query with JOIN FETCH
@Query("SELECT m FROM Medicao m JOIN FETCH m.usuario")
List<Medicao> findAllWithUsuario();

// Or use @EntityGraph
@EntityGraph(attributePaths = "usuario")
List<Medicao> findAll();
```

### 2. Database Indexing

```java
@Entity
@Table(name = "medicoes", indexes = {
    @Index(name = "idx_usuario_data", columnList = "usuario_id, data_medicao"),
    @Index(name = "idx_usuario_id", columnList = "usuario_id"),
    @Index(name = "idx_data_medicao", columnList = "data_medicao")
})
public class Medicao {
    // ...
}

@Entity
@Table(name = "usuarios", indexes = {
    @Index(name = "idx_email_unique", columnList = "email", unique = true),
    @Index(name = "idx_data_criacao", columnList = "data_criacao")
})
public class Usuario {
    // ...
}
```

### 3. Pagination for Large Result Sets

```java
// ❌ Bad: Loading all measurements at once
List<Medicao> todas = repositorio.findByUsuarioId(userId);

// ✅ Good: Paginated queries
@GetMapping("/medicoes")
public ResponseEntity<Page<MedicaoDTO>> listarPaginado(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        Authentication auth) {
    Usuario usuario = obterUsuarioAutenticado(auth);
    Page<Medicao> medicoes = repositorio.findByUsuarioId(
        usuario.getId(),
        PageRequest.of(page, size, Sort.by("dataMedicao").descending())
    );
    return ResponseEntity.ok(medicoes.map(MedicaoDTO::new));
}

// Interface
@Repository
public interface RepositorioMedicao extends JpaRepository<Medicao, Long> {
    Page<Medicao> findByUsuarioId(Long usuarioId, Pageable pageable);
}
```

### 4. Lazy Loading vs Eager Loading

```java
// Default: LAZY loading (better for most cases)
@ManyToOne(fetch = FetchType.LAZY)
private Usuario usuario;

// Only use EAGER if you ALWAYS need it
@ManyToOne(fetch = FetchType.EAGER)
private Usuario usuario;
```

### 5. Query Projection (SELECT only needed columns)

```java
// ❌ Bad: Loading entire entity
List<Medicao> todas = repositorio.findAll();

// ✅ Good: Project only needed columns
@Query("SELECT new com.app.gym.MedicaoSummaryDTO(m.id, m.peso, m.dataMedicao) " +
       "FROM Medicao m WHERE m.usuario.id = ?1")
List<MedicaoSummaryDTO> findSummaryByUsuarioId(Long usuarioId);
```

## Caching

### 1. Simple Cache Configuration

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("usuarios", "medicoes", "stats");
    }
}
```

### 2. Cache Annotations

```java
@Service
public class ServicoUsuario {
    
    // Cache result for 5 minutes
    @Cacheable(value = "usuarios", key = "#id")
    public Usuario obterPorId(Long id) {
        return repositorio.findById(id).orElse(null);
    }
    
    // Evict cache when updating
    @CacheEvict(value = "usuarios", key = "#usuario.id")
    public Usuario atualizar(Usuario usuario) {
        return repositorio.save(usuario);
    }
    
    // Clear all cache in value
    @CacheEvict(value = "medicoes", allEntries = true)
    public Medicao registrarMedicao(Medicao medicao) {
        return repositorio.save(medicao);
    }
}
```

### 3. Distributed Cache (Redis)

```yaml
# application.properties
spring.cache.type=redis
spring.redis.host=localhost
spring.redis.port=6379
spring.cache.redis.time-to-live=300000 # 5 minutes
```

## API Performance

### 1. Response Compression

```yaml
# application.properties
server.compression.enabled=true
server.compression.min-response-size=1024
server.compression.mime-types=application/json,application/xml,text/html,text/xml,text/plain
```

### 2. API Rate Limiting

```java
@Configuration
public class RateLimitConfig {
    
    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.create(10.0); // 10 requests per second
    }
}

@RestController
public class ControladorMedicoes {
    
    private final RateLimiter limiter;
    
    @GetMapping("/medicoes")
    public ResponseEntity<?> listar(Authentication auth) {
        if (!limiter.tryAcquire()) {
            return ResponseEntity.status(429).build(); // Too Many Requests
        }
        // ... rest of logic
    }
}
```

### 3. Async Processing

```java
@Service
public class ServicoEmail {
    
    @Async
    public void enviarEmailAssincrono(String email, String assunto) {
        // This runs in a separate thread
        // Don't block the main request
    }
}

// Usage
@PostMapping("/usuarios")
public ResponseEntity<UsuarioDTO> registrar(@Valid @RequestBody RegistroDTO dto) {
    Usuario usuario = servico.registrar(dto);
    servicoEmail.enviarEmailAssincrono(usuario.getEmail(), "Bem-vindo!");
    return ResponseEntity.status(201).body(new UsuarioDTO(usuario));
}
```

## Frontend Performance

### 1. API Response Filtering

```java
// ❌ Bad: Return all fields
@GetMapping("/medicoes")
public List<Medicao> listar() {
    return repositorio.findAll();
}

// ✅ Good: Return only needed fields
@GetMapping("/medicoes")
public List<MedicaoDTO> listar() {
    return repositorio.findAll().stream()
        .map(MedicaoDTO::new)
        .collect(Collectors.toList());
}

// Or use @JsonView
public class MedicaoDTO {
    @JsonView(Views.Public.class)
    public Long id;
    
    @JsonView(Views.Public.class)
    public Double peso;
    
    @JsonView(Views.Admin.class)
    public String usuarioEmail; // Only for admin
}
```

### 2. Lazy Load Images/Charts

```html
<!-- Defer chart loading until visible -->
<canvas id="chart" data-lazy="true"></canvas>

<script>
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                loadChart(entry.target);
                observer.unobserve(entry.target);
            }
        });
    });
    
    document.querySelectorAll('[data-lazy]').forEach(el => observer.observe(el));
</script>
```

### 3. Client-side Caching

```javascript
// Cache graph data for 5 minutes
const cache = new Map();

async function fetchGraficoComCache(url) {
    if (cache.has(url)) {
        const cached = cache.get(url);
        if (Date.now() - cached.timestamp < 5 * 60 * 1000) {
            return cached.data;
        }
    }
    
    const data = await fetch(url).then(r => r.json());
    cache.set(url, { data, timestamp: Date.now() });
    return data;
}
```

## Monitoring & Profiling

### 1. Application Metrics

```yaml
# Add Micrometer for metrics
# pom.xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

```properties
# application.properties
management.endpoints.web.exposure.include=health,metrics,prometheus
management.metrics.export.prometheus.enabled=true
```

### 2. Slow Query Logging

```yaml
# application.properties
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
spring.jpa.properties.hibernate.generate_statistics=true
spring.jpa.properties.hibernate.use_sql_comments=true
```

### 3. Profiling Code

```java
public class PerformanceMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);
    
    public static <T> T medirTempo(String operacao, Supplier<T> funcao) {
        long inicio = System.currentTimeMillis();
        try {
            return funcao.get();
        } finally {
            long duracao = System.currentTimeMillis() - inicio;
            if (duracao > 1000) { // Log if > 1s
                logger.warn("SLOW: {} levou {}ms", operacao, duracao);
            } else {
                logger.debug("{} levou {}ms", operacao, duracao);
            }
        }
    }
}

// Usage
List<Medicao> medicoes = PerformanceMonitor.medirTempo(
    "Carregar medições",
    () -> servico.obterMedicoes(usuarioId)
);
```

## Load Testing

```bash
# Using Apache JMeter
# Create a test plan:
# - Thread Group: 100 users, ramp-up 10 seconds
# - HTTP Sampler: GET /api/medicoes
# - Listener: View Results Tree
```

## Optimization Checklist

### Database
- [ ] Indexes on frequently queried columns
- [ ] No N+1 queries (use JOIN FETCH)
- [ ] Pagination for large datasets
- [ ] Appropriate fetch strategies (LAZY vs EAGER)
- [ ] Query projections to load only needed data
- [ ] Connection pooling configured

### Caching
- [ ] Cache strategy defined for high-traffic endpoints
- [ ] Cache invalidation on data updates
- [ ] TTL configured appropriately
- [ ] Cache hit rates monitored

### API
- [ ] Response compression enabled
- [ ] Rate limiting on sensitive endpoints
- [ ] Async processing for long operations
- [ ] DTO filtering (return only needed fields)
- [ ] Pagination support

### Frontend
- [ ] Lazy loading for images/charts
- [ ] Client-side caching
- [ ] Minimize HTTP requests
- [ ] CSS/JS minification

### Monitoring
- [ ] Application metrics exposed
- [ ] Slow query logging enabled
- [ ] Performance alerts configured
- [ ] Regular load testing

## Benchmark Results

| Operation | Current | Target | Status |
|-----------|---------|--------|--------|
| Login | 200ms | <500ms | ✓ Good |
| Load measurements | 500ms | <1s | ✓ Good |
| Generate chart data | 800ms | <2s | ✓ Good |
| Register user | 300ms | <1s | ✓ Good |

## Questions to Ask AI

- "Analyze this query for N+1 problems"
- "Show me how to implement caching for this endpoint"
- "Generate performance tests for my API"
- "What's the best way to optimize this chart loading?"
- "Help me set up monitoring for slow queries"

## Resources

- [Spring Data JPA Performance](https://spring.io/projects/spring-data-jpa)
- [Hibernate Performance Tuning](https://hibernate.org/orm/documentation/)
- [Database Indexing Best Practices](https://use-the-index-luke.com/)
- [Load Testing with JMeter](https://jmeter.apache.org/)
