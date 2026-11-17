# Gerenciador de Tarefas

API REST desenvolvida com Spring Boot para a disciplina de Backend.

## Tecnologias
Java 17, Spring Boot 3, Spring Data JPA, Bean Validation, H2, Lombok, springdoc-openapi (Swagger).

## Como executar
```bash
mvn spring-boot:run
```
- Swagger UI: http://localhost:8080/swagger-ui.html
- Console H2: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:tarefasdb`, usuário `sa`, senha vazia)

## Arquitetura
`controller` -> `service` -> `repository` -> banco, com `dto` para entrada/saída e `exception` para tratamento global de erros.

## Entidades e relacionamentos
- **Usuario** 1 --- N **Tarefa**
- **Categoria** 1 --- N **Tarefa**

## Endpoints
| Método | Rota | Descrição |
|---|---|---|
| POST/GET | `/usuarios` | Criar / listar usuários |
| GET/PUT/DELETE | `/usuarios/{id}` | Buscar / atualizar / excluir |
| POST/GET | `/categorias` | Criar / listar categorias |
| GET/PUT/DELETE | `/categorias/{id}` | Buscar / atualizar / excluir |
| POST | `/tarefas` | Criar tarefa |
| GET | `/tarefas?status=&usuarioId=&page=&size=` | Listar com filtro e paginação |
| GET/PUT/DELETE | `/tarefas/{id}` | Buscar / atualizar / excluir |
| PATCH | `/tarefas/{id}/status?novo=CONCLUIDA` | Alterar status |

## Regras de negócio
- E-mail de usuário e nome de categoria são únicos.
- Não é possível excluir usuário ou categoria que possua tarefas.
- Tarefas concluídas não podem ser editadas nem mudar de status.
- A data limite não pode estar no passado.

## Códigos HTTP
200 (ok), 201 (criado), 204 (excluído), 400 (dados inválidos), 404 (não encontrado), 422 (regra de negócio violada).

## Interface web

https://gerenciador-de-tarefas-dvbh.onrender.com/
