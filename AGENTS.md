# Guia de Desenvolvimento do Projeto Gym

Atençao : Nome das classes e metodos em pt-br 
Faça comentarios anteriores ao metodos explicando seu comportamento/função

## Objetivo
Este projeto utiliza Java 21, Spring Boot, Thymeleaf e arquitetura hexagonal e banco de dados local h2.

Se trata de um aplicativo fitness que permite cadastro de usuario e cadastro de alimentos vide 2 ocaoes 100g ou porçao (1 fatia) e a caloria equivalente


## Princípios de Arquitetura
- Arquitetura Hexagonal
- Separar o código em camadas claras:
  - domain: regras de negócio e entidades
  - application: casos de uso e serviços
  - infrastructure: controllers, persistência, integração externa
- Evitar dependências de infraestrutura no domínio.
- Manter a lógica de negócio centralizada e independente de frameworks.

## Stack Tecnológica
- Java 21
- Maven
- Spring Boot
- Thymeleaf
- H2 para ambiente local
- Spring Security para autenticação futura
- JUnit 5 + Mockito para testes unitários

## Convenções
- Nomear classes com significado de negócio.
- Preferir nomes como GymAppService em vez de nomes genéricos.
- Criar interfaces de porta quando houver dependência externa.
- Manter controllers finos, com lógica mínima.

## Estrutura sugerida
- src/main/java/com/gym/domain
- src/main/java/com/gym/gym/application
- src/main/java/com/gym/gym/infrastructure
- src/main/resources/templates

## Regras de implementação
- Nome das classes sempre em pt-br
- O domínio não deve depender de Spring diretamente.
- Os serviços de aplicação podem usar interfaces para abstrair repositórios.
- Os controllers devem orquestrar chamadas de aplicação e retornar views ou respostas HTTP.
- Sempre trate erros de operações inválidas ou duplicadas de forma explícita, com mensagens claras para o usuário e sem falhas silenciosas.
- Para futuras evoluções, preparar autenticação, 2FA e persistência.
- Sempre se preocupe com cobertura de testes com 90% ou mais de cobertura

# Endpoints disponíveis
- GET `/login` (view login)
- POST `/login` (autenticação via Spring Security)
- GET `/register` (view cadastro)
- POST `/register` (cria usuário)
- GET `/home` (view após login)
- POST `/logout` (logout)
- GET `/h2-console` (console H2 em memória)
- POST `/api/auth/login` (gera JWT para acesso à API)
- GET `/api/**` (exigirá autenticação)

# Próximos passos
- Criar autenticação com cadastro e login
- Adicionar 2FA
- Substituir H2 por DynamoDB em ambiente mais real

