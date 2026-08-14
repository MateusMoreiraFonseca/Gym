# Security Checklist

## Objective
Ensure the authentication and data handling follow OWASP Top 10 and fitness app security best practices.

## Critical (Do Not Deploy Without Fixing)

### 1. Authentication & Session Management

- [ ] **Password Storage**: Passwords hashed with BCrypt with cost ≥ 10
  ```java
  // ✅ Good
  BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
  String hashedPassword = encoder.encode(rawPassword);
  ```

- [ ] **JWT Token Expiration**: Tokens expire in reasonable time (15-60 minutes)
  ```java
  // ✅ Good
  long expirationTime = System.currentTimeMillis() + (1000 * 60 * 15); // 15 minutes
  ```

- [ ] **Token Secret**: JWT secret is strong and NOT hardcoded
  ```yaml
  # ✅ Good: In application.properties
  jwt.secret=${JWT_SECRET}  # Injected from environment
  jwt.expiration=900000     # 15 minutes in milliseconds
  ```

- [ ] **Account Lockout**: After N failed login attempts, lock account temporarily
  ```java
  // ✅ Pattern
  if (failedAttempts > MAX_ATTEMPTS) {
      usuario.setBloqueado(true);
      usuario.setDataBloqueio(LocalDateTime.now());
  }
  ```

### 2. Input Validation

- [ ] **Email Validation**: Proper email format check
  ```java
  @Email(message = "Email inválido")
  private String email;
  ```

- [ ] **Password Validation**: Minimum length, complexity requirements
  ```java
  private static final int TAMANHO_MINIMO_SENHA = 8;
  // Password must contain: uppercase, lowercase, digit, special char
  ```

- [ ] **SQL Injection Prevention**: Use JPA with parameterized queries (NOT string concatenation)
  ```java
  // ❌ Bad: Vulnerable
  Query query = em.createQuery("SELECT u FROM Usuario u WHERE email = '" + email + "'");
  
  // ✅ Good: Safe
  Query query = em.createQuery("SELECT u FROM Usuario u WHERE email = :email");
  query.setParameter("email", email);
  ```

### 3. Data Protection

- [ ] **No Sensitive Data in Logs**: Don't log passwords, tokens, medical data
  ```java
  // ❌ Bad
  logger.info("User login: " + usuario.getSenha());
  
  // ✅ Good
  logger.info("User logged in: {}", usuario.getId());
  ```

- [ ] **No Sensitive Data in JWT**: Don't include passwords, medical history
  ```java
  // ❌ Bad
  claims.put("senha", usuario.getSenha());
  
  // ✅ Good
  claims.put("userId", usuario.getId());
  claims.put("email", usuario.getEmail());
  ```

- [ ] **Secure Password Reset**: Token-based, time-limited (not security questions)
  ```java
  // ✅ Pattern
  @Entity
  public class TokenResetSenha {
      private String token;
      private Long usuarioId;
      private LocalDateTime dataCriacao;
      private LocalDateTime dataExpiracao; // Max 30 minutes
  }
  ```

### 4. Authorization

- [ ] **Role-Based Access Control**: Users can only access their own data
  ```java
  // ✅ Good
  @GetMapping("/api/medicoes")
  public List<Medicao> obterMedicoes(Authentication auth) {
      String emailUsuario = auth.getName();
      return servico.obterMedicoesDoUsuario(emailUsuario);
  }
  ```

- [ ] **No Direct Object References**: Don't expose internal IDs without authorization
  ```java
  // ✅ Good
  @GetMapping("/api/medicoes/{id}")
  public Medicao obterMedicao(@PathVariable Long id, Authentication auth) {
      Medicao medicao = repositorio.findById(id);
      if (!medicao.getUsuario().getEmail().equals(auth.getName())) {
          throw new AccessDeniedException("Acesso negado");
      }
      return medicao;
  }
  ```

## High Priority (Fix Before Production)

### 5. CORS Configuration

- [ ] **Restrictive CORS**: Specify allowed origins, not wildcard
  ```java
  // ❌ Bad
  .allowCredentials(true)
  .allowedOrigins("*")
  
  // ✅ Good
  .allowedOrigins("https://meuapp.com", "https://www.meuapp.com")
  .allowedMethods("GET", "POST", "PUT", "DELETE")
  .allowCredentials(false)  // or true only with specific origins
  ```

- [ ] **No Credentials Over HTTP**: Enforce HTTPS
  ```java
  // ✅ Good
  http.requiresChannel().anyRequest().requiresSecure()
  ```

### 6. Error Handling

- [ ] **No Stack Traces to Client**: Don't expose internal errors
  ```java
  // ❌ Bad
  catch (Exception e) {
      return ResponseEntity.status(500)
          .body(new ErrorDTO(e.getStackTrace()));
  }
  
  // ✅ Good
  catch (Exception e) {
      logger.error("Unexpected error", e);
      return ResponseEntity.status(500)
          .body(new ErrorDTO("Erro interno do servidor"));
  }
  ```

- [ ] **Consistent Error Messages**: Don't reveal if user exists
  ```java
  // ❌ Bad
  if (usuario == null) {
      return "Usuário não encontrado";
  }
  if (!senhaValida) {
      return "Senha inválida";
  }
  
  // ✅ Good
  if (usuario == null || !senhaValida) {
      return "Email ou senha inválidos";
  }
  ```

### 7. Rate Limiting

- [ ] **Login Attempts Limited**: Prevent brute force attacks
  ```java
  // ✅ Pattern: Track failed attempts per IP/email
  private Map<String, Integer> tentativasFalhadas = new ConcurrentHashMap<>();
  
  if (tentativasFalhadas.getOrDefault(email, 0) >= MAX_ATTEMPTS) {
      throw new ContaBloqueadaException("Muitas tentativas. Tente novamente depois.");
  }
  ```

## Medium Priority (Good Security Practice)

### 8. Audit Logging

- [ ] **Log Security Events**: Track logins, password changes, data access
  ```java
  // ✅ Good
  @Entity
  public class LogSeguranca {
      private Long usuarioId;
      private String evento; // "LOGIN", "LOGOUT", "SENHA_ALTERADA", "MEDICAO_ADICIONADA"
      private LocalDateTime dataHora;
      private String ipOrigem;
  }
  ```

### 9. Cryptography

- [ ] **Use Standard Libraries**: Don't implement custom encryption
  ```java
  // ✅ Good: Use Spring Security's BCryptPasswordEncoder
  BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
  ```

- [ ] **Random Token Generation**: Use SecureRandom
  ```java
  // ✅ Good
  SecureRandom random = new SecureRandom();
  String token = Base64.getUrlEncoder().encodeToString(random.generateSeed(32));
  ```

### 10. Data Privacy

- [ ] **PII Protection**: Personal health information is sensitive
  - Encrypt sensitive fields at rest (weight, body measurements)
  - Limit access to authorized personnel
  - Implement data retention policies

- [ ] **GDPR/Privacy Compliance**: Users can request/delete their data
  ```java
  // ✅ Pattern
  @DeleteMapping("/api/usuarios/{id}")
  public void deletarConta(@PathVariable Long id) {
      usuario = verificarAutorizacao(id);
      servicoExlusao.apagarDadosUsuario(id);
  }
  ```

## Validation Checklist

- [ ] No hardcoded secrets (API keys, JWT secrets, passwords)
- [ ] Secrets in environment variables or secure config
- [ ] No sensitive data in logs
- [ ] All user inputs validated
- [ ] SQL injection prevented (parameterized queries)
- [ ] CORS properly configured
- [ ] Password requirements enforced
- [ ] JWT tokens have expiration
- [ ] Error messages don't leak information
- [ ] HTTPS enforced in production
- [ ] Rate limiting on sensitive endpoints
- [ ] Audit logging for security events
- [ ] Authorization checks on all endpoints
- [ ] No direct object references without auth

## Testing Security

- [ ] Security test cases for each endpoint
- [ ] Test invalid input: null, empty, special characters, SQL injection
- [ ] Test authorization: unauthorized users rejected
- [ ] Test rate limiting: after N attempts, blocked
- [ ] Test token expiration: expired tokens rejected

## Questions to Ask AI

- "Review my security configuration and identify vulnerabilities"
- "Show me how to implement rate limiting for login"
- "Generate security tests for this endpoint"
- "How should I handle password reset securely?"
- "Implement audit logging for authentication events"

## Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Best Practices](https://spring.io/projects/spring-security)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8949)
