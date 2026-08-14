---
name: fitness-ai-learning
description: 'Learn to code the Gym Fitness application using AI. Use when building fitness features (authentication, measurements, calculations, graphs). Provides step-by-step workflows with quality gates, testing strategies, and code review practices.'
argument-hint: 'Feature or component to build (e.g., "authentication review", "add measurements", "create graphs")'
user-invocable: true
---

# Fitness App Development with AI

A structured learning workflow for building the Gym Fitness application with AI guidance. Combines hands-on coding, testing practices, and code quality standards.

## Overview

This skill guides development of the Gym Fitness app through multiple learning phases:

1. **Phase 1: Foundation** – Review & strengthen existing authentication system
2. **Phase 2: Core Features** – Build measurements & calculations
3. **Phase 3: Visualization** – Implement graphs and dashboards
4. **Phase 4: Polish** – Performance, security, deployment

## When to Use

- Building a new feature from scratch
- Reviewing existing code for quality/security
- Adding unit tests or integration tests
- Debugging or refactoring components
- Learning Spring Boot + Java patterns for fitness domain
- Preparing code for production

## Phase 1: Authentication & Foundation (CURRENT)

### Objective
Validate and strengthen the existing login/registration system. Learn testing and code quality best practices.

### Procedure

1. **Code Review** – Review existing authentication code
   - Review [Authentication Checklist](./references/authentication-checklist.md)
   - Examine: `ControladorAutenticacao.java`, `ServicoAutenticacao.java`, `ConfiguracaoSeguranca.java`
   - Identify security gaps and code quality issues

2. **Write Unit Tests**
   - Use [Testing Strategy](./references/test-strategy.md)
   - Reference existing: `AuthControllerTest.java`, `ControladorAutenticacaoApiTest.java`, `ContaUsuarioTest.java`
   - Add tests for: JWT token generation, password encryption, user registration validation

3. **Apply Best Practices**
   - Use [Code Quality Guide](./references/code-quality-practices.md)
   - Refactor for: error handling, logging, validation, exception handling
   - Ensure: input validation, SQL injection prevention, secure password storage

4. **Security Hardening**
   - Follow [Security Checklist](./references/security-checklist.md)
   - Validate: JWT expiration, CORS configuration, password complexity
   - Add: rate limiting, account lockout, audit logging

5. **Quality Gate** ✓
   - All unit tests passing (>80% coverage)
   - Security scan passed (no CVE, no hardcoded secrets)
   - Code review complete (no high-priority issues)

## Phase 2: Measurements & Calculations

### Objective
Build the core fitness tracking domain: user measurements with automatic calculations (BMI, body composition).

### Procedure

1. **Design Domain Model**
   - Create `Medicao` (Measurement) entity
   - Design `ServicoCalculoMedicao` (Calculation Service)
   - Reference: [Domain Design Template](./references/domain-template.md)

2. **Implement Calculations**
   - BMI: `peso / (altura ^ 2)`
   - Body composition percentages
   - Trend analysis logic

3. **Build REST API**
   - POST `/api/medicoes` – Record new measurement
   - GET `/api/medicoes` – List user measurements
   - GET `/api/medicoes/{id}` – Get measurement details

4. **Write Tests**
   - Unit tests for calculations (edge cases, validation)
   - Integration tests for API endpoints
   - Test data scenarios

5. **Quality Gate** ✓
   - All calculations verified against fitness formulas
   - API contract tests passing
   - Validations for impossible measurements

## Phase 3: Graphs & Visualization

### Objective
Display measurement trends over time with charts (weight, BMI, body measurements).

### Procedure

1. **Backend: Aggregation & Trends**
   - Create aggregation queries for chart data
   - Implement time-series grouping (by day, week, month)
   - Reference: [Graph Data Template](./references/graph-data-template.md)

2. **Frontend: Chart Integration**
   - Add charting library (Chart.js or similar)
   - Create Thymeleaf templates for dashboard
   - Display: weight trend, BMI progression, measurement history

3. **API Endpoints for Graphs**
   - GET `/api/medicoes/trends?period=week` – Aggregated data
   - GET `/api/medicoes/stats` – Summary statistics

4. **Quality Gate** ✓
   - Charts render correctly with sample data
   - No N+1 queries (optimized database queries)
   - Responsive design on mobile

## Phase 4: Polish & Production

### Objective
Performance, monitoring, deployment readiness.

### Procedure

1. **Performance Optimization**
   - Profile database queries
   - Add caching where appropriate
   - Reference: [Performance Checklist](./references/performance-checklist.md)

2. **Monitoring & Logging**
   - Structure logs by component
   - Add application metrics
   - Error alerting

3. **Documentation**
   - API documentation (Swagger/OpenAPI)
   - User guide for features

4. **Deployment Preparation**
   - Docker containerization
   - Environment configuration
   - Database migration scripts

## AI-Assisted Workflow

Each phase follows a pattern:

**DESIGN** → **IMPLEMENT** → **TEST** → **REVIEW** → **QUALITY GATE**

### At each step, ask AI to:

1. **DESIGN**: "Help me design the API contract for [feature]"
   - AI generates: endpoint spec, data models, error responses

2. **IMPLEMENT**: "Generate code for [specific component]"
   - AI generates: starter code, helper methods, utilities
   - You review, adapt, integrate

3. **TEST**: "Generate unit tests for [functionality]"
   - AI generates: test cases with edge cases
   - You run, adapt, ensure coverage

4. **REVIEW**: "Review this code for [quality concern]"
   - AI identifies: improvements, antipatterns, risks
   - You apply fixes

5. **QUALITY GATE**: "Verify this passes the quality gate for [phase]"
   - AI validates: tests, security, standards
   - You merge to main

## Technology Stack

- **Backend**: Java 21+, Spring Boot 3.x
- **Security**: JWT, Spring Security
- **Database**: (check `application.properties`)
- **Frontend**: Thymeleaf, CSS, JavaScript
- **Testing**: JUnit 5, Mockito
- **Build**: Maven

## Project Structure

```
src/
├── main/java/com/app/gym/
│   ├── application/     # Services, business logic
│   ├── domain/          # Entities, value objects
│   └── infrastructure/  # Controllers, external integrations
└── test/java/...        # Unit and integration tests
```

## References

- [Authentication Checklist](./references/authentication-checklist.md)
- [Code Quality Guide](./references/code-quality-practices.md)
- [Security Checklist](./references/security-checklist.md)
- [Testing Strategy](./references/test-strategy.md)
- [Domain Design Template](./references/domain-template.md)
- [Graph Data Template](./references/graph-data-template.md)
- [Performance Checklist](./references/performance-checklist.md)

## Next Steps

1. Start with [Authentication Checklist](./references/authentication-checklist.md)
2. Run existing tests: `mvn test`
3. Ask AI: "Review the authentication code in this project using the fitness-ai-learning skill"
4. Complete Phase 1 quality gate before moving to Phase 2
