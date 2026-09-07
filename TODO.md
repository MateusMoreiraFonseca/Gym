# TODO

## Entregas concluídas

- [x] Implementar autenticação, cadastro, perfil e administração.
- [x] Adicionar calendário mensal na tela inicial.
- [x] Permitir seleção de um dia e exibir medições e treinos do dia.
- [x] Cadastrar treinos com recorrência diária, semanal ou em data única.
- [x] Registrar peso, altura e calcular IMC.
- [x] Registrar kcal ingeridas e gastas por dia.
- [x] Calcular metabolismo basal por Mifflin-St Jeor e Harris-Benedict.
- [x] Exibir média do metabolismo basal, gasto total, meta e saldo calórico.
- [x] Permitir carregar foto de perfil e exibi-la como avatar no topo.
- [x] Criar página de perfil com upload de foto e acesso à edição da conta.
- [x] Validar o projeto com `mvn test` — 60 testes verdes.

## Próxima evolução

- [ ] Adicionar testes unitários para `ServicoCalorico` e `ServicoTreino`.
- [ ] Adicionar endpoints REST de perfil calórico, registros calóricos e treinos.
- [ ] Permitir editar e excluir treinos cadastrados.
- [ ] Adicionar histórico gráfico de peso, IMC e saldo calórico.
- [ ] Permitir mais de um lançamento de refeição/exercício por dia, mantendo totais.
- [ ] Persistir configurações em banco de produção e criar migrações.
- [ ] Externalizar `jwt.secret` e a senha do administrador por variáveis de ambiente.
