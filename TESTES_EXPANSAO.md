# Testes — Plano de expansão (Fase 1)

**Atualizado em:** 07/09/2026  
**Status:** Suíte ativa e verde — **60 testes, 0 falhas, 0 erros**.

## Cobertura funcional atual

| Área | Arquivo(s) de teste | Cenários cobertos | Estado |
|---|---|---|---|
| Entidade de usuário | `ContaUsuarioTest` | Criação de usuário, administrador e `id` não persistido | Concluído |
| Cadastro | `ServicoAutenticacaoTest`, `ControladorAutenticacaoTest` | Sucesso, duplicidade, dados nulos/vazios, BCrypt, papéis e persistência | Concluído |
| JWT | `ServicoJwtTest` | Geração, extração de usuário, token válido, vazio, nulo, malformado e adulterado | Concluído |
| Filtro JWT | `FiltroAutenticacaoJwtTest` | Bearer válido, token inválido, cabeçalho ausente/incorreto e usuário não encontrado | Concluído |
| Login de API | `ControladorAutenticacaoApiTest` | Credenciais válidas, senha inválida e usuário inexistente | Concluído |
| Serviços auxiliares | `ServicosAuxiliaresTest` | Criptografia, listagem e semente do administrador | Concluído |
| Perfil e administração | `ControladoresPerfilEAdminTest` | Perfil, troca de senha, autorização e exclusão de usuário comum | Concluído |
| Medições fitness | `ServicoMedicaoTest` | Cálculo, classificação de IMC e rejeição de medidas inválidas | Concluído |

## Verificação

```powershell
mvn test
```

Resultado esperado:

```text
Tests run: 60, Failures: 0, Errors: 0, Skipped: 0
```

## Próximos testes a implementar

### Validação de entrada

- [x] `POST /api/auth/login` com campos vazios e JSON malformado retorna `400`.
- [x] `POST /register` com username ou senha somente com espaços retorna mensagem de validação.
- [x] Formulário de cadastro com tentativa de `isAdmin=true` por usuário comum continua criando um usuário comum.

### Perfil e administração

- [x] Perfil: nova senha vazia retorna a mensagem apropriada.
- [ ] Perfil: usuário autenticado inexistente produz erro controlado.
- [ ] Administração: usuário não administrador não pode excluir contas.
- [x] Administração: conta inexistente não é excluída.
- [x] Administração: administrador não pode excluir outra conta administrativa.

### Segurança e integração

- [ ] Endpoint protegido de API com JWT válido retorna sucesso em uma rota de API existente.
- [ ] Endpoint protegido de API sem token ou token inválido retorna JSON `401`, após configurar o tratamento específico para `/api/**`.
- [ ] Fluxo completo: cadastrar → login API → chamar endpoint de API protegido.
- [ ] Testes de CORS, rate limiting e bloqueio de conta, quando esses recursos forem implementados.

### Fitness

- [ ] `ServicoCalorico`: Mifflin-St Jeor para os dois sexos.
- [ ] `ServicoCalorico`: Harris-Benedict para os dois sexos.
- [ ] `ServicoCalorico`: média basal, fator de atividade e ajuste de 500 kcal.
- [ ] `ServicoCalorico`: saldo, déficit, superávit e registro diário.
- [ ] `ServicoTreino`: recorrência diária, semanal e data única.
- [ ] `ControladorHome`: seleção de datas e dados do dia.

## Medição de cobertura

O JaCoCo está configurado no `pom.xml` para instrumentar os testes e gerar o relatório em `target/site/jacoco/index.html`. A cobertura deve ser recalculada após a inclusão dos testes de calorias e treinos.

Quando o acesso ao artefato estiver disponível, configurar JaCoCo e adotar esta meta:

| Indicador | Meta |
|---|---:|
| Cobertura de linhas da lógica de aplicação | >= 80% |
| Cobertura de ramos da lógica de autenticação | >= 80% |
| Testes verdes | 100% |
