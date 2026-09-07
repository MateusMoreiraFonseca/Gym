# Phase 1 — Autenticação e administração

**Atualizado em:** 07/09/2026  
**Status:** Fase 1 concluída funcionalmente; Fase 2 em evolução — testes verdes.

## Situação atual

- Cadastro de usuários via `GET`/`POST /register`.
- Login de interface com Spring Security e login de API em `POST /api/auth/login`.
- JWT assinado, com expiração e filtro de autenticação.
- Perfil de usuário com alteração de senha.
- Administração de usuários protegida por `ROLE_ADMIN`.
- A tela `/home` exibe um calendário fitness mensal protegido por autenticação.
- Cada dia permite consultar medições, IMC e treinos programados.
- Treinos podem ser únicos, diários ou recorrentes em um dia da semana.
- A tela `/calorias` calcula metabolismo basal, gasto total, meta e saldo calórico.
- O serviço bloqueia a criação de administradores por usuários comuns, mesmo que o formulário seja alterado no navegador.
- Administrador inicial criado automaticamente pela `ServiçoSementeAdministrador`.
- Suíte atual: **60 testes executados, 0 falhas e 0 erros** (`mvn test`).

## Fase 2 — Funcionalidades fitness

### Concluído

- [x] Criar entidade de medição por usuário com cálculo de IMC.
- [x] Criar calendário mensal com seleção de datas.
- [x] Criar entidade de treino com recorrência diária, semanal e única.
- [x] Criar perfil fitness com idade, sexo, altura, peso atual, meta e atividade.
- [x] Calcular metabolismo basal por Mifflin-St Jeor e Harris-Benedict.
- [x] Exibir a média das fórmulas e o gasto energético total estimado.
- [x] Registrar kcal ingeridas e kcal gastas por data.
- [x] Calcular meta com ajuste gradual de 500 kcal para ganho ou perda de peso.

### Próximo ciclo

- [ ] Cobrir fórmulas e limites de entrada com testes unitários.
- [ ] Separar lançamentos de refeições e exercícios em eventos diários.
- [ ] Exibir gráficos de peso, IMC e saldo calórico.
- [ ] Adicionar edição e exclusão de treinos.

## Concluído

- [x] Ativar e corrigir os testes que estavam comentados.
- [x] Cobrir cadastro válido, duplicado e dados vazios.
- [x] Cobrir geração, leitura e rejeição de tokens JWT inválidos.
- [x] Cobrir o filtro JWT com token válido, inválido, ausente e usuário inexistente.
- [x] Cobrir login de API válido, senha inválida e usuário inexistente.
- [x] Cobrir criptografia de senha, listagem de usuários e semente do administrador.
- [x] Cobrir os principais fluxos de perfil e exclusão administrativa.
- [x] Restringir os atalhos administrativos da tela inicial a `ROLE_ADMIN`.

## Próximas ações prioritárias

### 1. Segurança de credenciais

- [ ] Mover `jwt.secret` para variável de ambiente, sem chave padrão em produção.
- [ ] Mover a senha do administrador inicial para `ADMIN_PASSWORD`.
- [ ] Desabilitar o console H2 fora do ambiente local.

### 2. Validação e proteção da API

- [ ] Validar explicitamente corpo de login ausente, vazio e JSON malformado.
- [ ] Definir resposta JSON `401`/`403` para rotas `/api/**`, em vez de redirecionamento ao formulário de login.
- [ ] Configurar CORS de modo explícito caso a API seja consumida por outro domínio.
- [ ] Implementar rate limiting para tentativas de login.
- [ ] Implementar bloqueio temporário após tentativas de autenticação falhas.

### 3. Qualidade e manutenção

- [ ] Remover a atualização de `id` por reflexão em `ControladorPerfil` e oferecer uma operação de domínio apropriada.
- [ ] Adicionar logs estruturados para eventos de autenticação, sem registrar senhas ou tokens.
- [ ] Adicionar testes para os ramos restantes dos controladores administrativos e validações de formulário.
- [x] Configurar o relatório JaCoCo no Maven.
- [x] Resolver o acesso aos artefatos JaCoCo no Maven Central e registrar a cobertura inicial.

## Critérios para concluir a fase

| Critério | Meta | Estado atual |
|---|---:|---|
| Testes automatizados | 100% verdes | 60/60 verdes |
| Cobertura mensurada | >= 80% | 89% de linhas |
| Segredos fora do repositório | 100% | Pendente |
| Validação de login | Completa | Parcial |
| Proteção contra força bruta | Implementada | Pendente |
| Fluxos de administração | Restritos a ADMIN | Concluído |

## Comandos

```powershell
# Executar a aplicação
mvn spring-boot:run

# Executar os testes
mvn test
```

## Próxima entrega recomendada

Externalizar os segredos e implementar validação completa no endpoint de login. Isso reduz os riscos mais relevantes antes de adicionar funcionalidades novas.
