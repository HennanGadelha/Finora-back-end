# Finora Backend

Backend do Finora, executado localmente com Java 21, Spring Boot, PostgreSQL e Docker Compose.

## Pré-requisitos

- Java 21 disponível no `PATH` ou configurado em `JAVA_HOME`.
- Docker Desktop com Docker Compose.
- No Windows, use `gradlew.bat`. Em Linux/macOS, use `./gradlew`.

## Variáveis de ambiente

Antes de iniciar a aplicação, preencha obrigatoriamente `JWT_SECRET`. A aplicação não fornece valor padrão para esse segredo.

| Variável | Obrigatória | Padrão local | Uso |
| --- | --- | --- | --- |
| `JWT_SECRET` | Sim | Nenhum | Segredo de assinatura dos tokens JWT. Use um valor local fora do repositório. |
| `DB_URL` | Não | `jdbc:postgresql://localhost:5432/finora` | URL JDBC do PostgreSQL. |
| `DB_USERNAME` | Não | `finora` | Usuário JDBC da aplicação. |
| `DB_PASSWORD` | Não | `finora` | Senha local do usuário JDBC. |
| `FLYWAY_ENABLED` | Não | `true` | Habilita migrations versionadas na inicialização. |
| `BCRYPT_STRENGTH` | Não | `12` | Fator de custo do BCrypt. |
| `JWT_EXPIRATION` | Não | `PT15M` | Duração do token JWT. |
| `FINORA_TEST_USER_ENABLED` | Não | `false` | Habilita a criação do usuário mockado local. |
| `FINORA_TEST_USER_NAME` | Não | `Usuario de Teste` | Nome do usuário mockado. |
| `FINORA_TEST_USER_EMAIL` | Não | `usuario.teste@finora.local` | E-mail do usuário mockado. |
| `FINORA_TEST_USER_PASSWORD` | Condicional | Nenhum | Obrigatória quando `FINORA_TEST_USER_ENABLED=true`. Não use senha real. |

As variáveis `POSTGRES_DB`, `POSTGRES_USER` e `POSTGRES_PASSWORD` controlam o container Docker e possuem padrão local `finora`. Se forem alteradas, mantenha `DB_USERNAME`, `DB_PASSWORD` e a base em `DB_URL` coerentes.

### Windows PowerShell

```powershell
$env:JWT_SECRET = "segredo-local-nao-versionado"
$env:FINORA_TEST_USER_ENABLED = "true"
$env:FINORA_TEST_USER_PASSWORD = "senha-local-fornecida-no-ambiente"
```

O usuário mockado é opcional. Se `FINORA_TEST_USER_ENABLED` permanecer `false`, não defina `FINORA_TEST_USER_PASSWORD`.

### Linux/macOS

```bash
export JWT_SECRET="segredo-local-nao-versionado"
export FINORA_TEST_USER_ENABLED=true
export FINORA_TEST_USER_PASSWORD="senha-local-fornecida-no-ambiente"
```

Não versionar arquivos `.env` com segredos reais. Para um ambiente compartilhado, forneça os valores pelo mecanismo de secrets apropriado.

## PostgreSQL local

Na pasta `finora/`, inicie o banco com:

```powershell
docker compose up -d --wait
```

No Linux/macOS:

```bash
docker compose up -d --wait
```

O Compose inicia PostgreSQL 16 na porta `5432`. Ao iniciar a aplicação, o Flyway aplica as migrations de `src/main/resources/db/migration`.

Para conferir os containers:

```powershell
docker compose ps
```

Para parar os serviços:

```powershell
docker compose down
```

Para parar e remover também os dados locais:

```powershell
docker compose down -v
```

## Executar a aplicação

Com `JWT_SECRET` preenchido e o PostgreSQL disponível:

```powershell
.\gradlew.bat bootRun
```

No Linux/macOS:

```bash
./gradlew bootRun
```

A aplicação inicia em `http://localhost:8080`.

Após a inicialização, a documentação está disponível em:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Contrato OpenAPI: `http://localhost:8080/v3/api-docs`

A Swagger UI é pública para permitir a consulta do contrato, mas os endpoints protegidos continuam exigindo Bearer JWT.

Sem um endpoint público de health check habilitado, confirme a disponibilidade observando a inicialização do Spring Boot ou fazendo uma requisição HTTP ao servidor:

```powershell
curl.exe -i http://localhost:8080/
```

Uma resposta HTTP, inclusive `404`, confirma que o servidor está escutando; não representa falha de inicialização por si só.

## Testes e Quality Gates

Execute a suíte completa e os Quality Gates com:

```powershell
.\gradlew.bat check
```

No Linux/macOS:

```bash
./gradlew check
```

Para executar somente os testes:

```powershell
.\gradlew.bat test
```

## Fluxo resumido

1. Preencha `JWT_SECRET`.
2. Inicie o PostgreSQL com Docker Compose.
3. Habilite o usuário mockado e preencha `FINORA_TEST_USER_PASSWORD` se precisar validar os endpoints autenticados.
4. Inicie a aplicação com o Gradle Wrapper.
5. Execute `check` antes de considerar uma alteração pronta.

A senha do usuário mockado é usada apenas para desenvolvimento/testes locais. Ela não é exibida pela API, registrada nos logs ou armazenada neste repositório em texto puro.
