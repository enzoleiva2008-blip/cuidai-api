# CuidAí API — "Seu cuidado marcado, sem fila."

API RESTful em **Java + Spring Boot** do CuidAí, a CitizenTech que permite ao cidadão agendar consultas na UBS, acompanhar a fila com prioridade (idoso, gestante, crônico) e receber lembretes, reduzindo faltas e espera.

Global Solution 2026/2 · Domain Driven Design Using Java · FIAP 1TDS

## Integrantes

| Nome | RM | Turma |
|---|---|---|
| Enzo Leiva | 568877 | 1TDSPO |
| Matheus Segura | 570878 | 1TDSPO |
| Kauã Fiuza | 572369 | 1TDSPO |
| Lucas Almeida | 572499 | 1TDSPO |

## Links

- **API no Render:** https://cuidai-api.onrender.com
- **Vídeo (YouTube, não listado):** _link_ ← trocar
- **Front-end:** https://cuidai-front.vercel.app · [repositório](https://github.com/enzoleiva2008-blip/cuidai-front)

## Tecnologias

Java 17 · Spring Boot 3.3 · JDBC puro (DAO + PreparedStatement) · Oracle · Bean Validation · ModelMapper · Docker · Render

> Sem JPA, Hibernate ou Spring Data: todo acesso ao banco está nos DAOs, com SQL escrito à mão.

## Estrutura

```
src/main/java/br/com/fiap/cuidai
├── model       entidades do domínio (Cidadao, Medico, Consulta, StatusConsulta)
├── dao         acesso ao Oracle com JDBC (DataSource no construtor + PreparedStatement)
├── dto         entrada (RequestDTO) e saída (ResponseDTO) da API
├── controller  rotas REST e status HTTP
├── exception   erros do domínio (RegraNegocio, HorarioOcupado, RecursoNaoEncontrado...)
├── handler     GlobalExceptionHandler: todo erro vira um JSON padronizado
└── config      CORS e ModelMapper
sql/cuidai.sql  criação das tabelas, sequences e dados de exemplo
```

## Como rodar

### 1. Banco
Rode `sql/cuidai.sql` no Oracle da FIAP (SQL Developer → F5).

### 2. Variáveis de ambiente
A API **não tem usuário nem senha no código**. Ela lê:

| Variável | Exemplo |
|---|---|
| `DB_URL` | `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` |
| `DB_USER` | seu RM (ex.: `rm123456`) |
| `DB_PASSWORD` | sua senha do Oracle |
| `CORS_ORIGINS` | (opcional) `https://cuidai.vercel.app,http://localhost:5173` |

**No IntelliJ:** Run › Edit Configurations › `CuidaiApplication` › *Environment variables* → `DB_URL=...;DB_USER=...;DB_PASSWORD=...`

### 3. Executar
- **IntelliJ:** rodar `CuidaiApplication` (precisa de JDK 17 ou mais novo).
- **Docker:**
  ```bash
  docker build -t cuidai-api .
  docker run -p 8080:8080 -e DB_URL=... -e DB_USER=... -e DB_PASSWORD=... cuidai-api
  ```

A API sobe em `http://localhost:8080`.

## Endpoints

### Cidadãos
| Método | Rota | Sucesso | Erros |
|---|---|---|---|
| POST | `/cidadaos` | 201 + Location | 400, 409 |
| GET | `/cidadaos` | 200 | — |
| GET | `/cidadaos/{id}` | 200 | 404 |
| PUT | `/cidadaos/{id}` | 200 | 400, 404, 409 |
| DELETE | `/cidadaos/{id}` | 204 | 404, 409 |
| GET | `/cidadaos/{id}/consultas` | 200 | 404 |
| POST | `/cidadaos/{id}/consultas` | 201 + Location | 400, 404, 409 |

### Médicos
| Método | Rota | Sucesso | Erros |
|---|---|---|---|
| POST | `/medicos` | 201 + Location | 400, 404, 409 |
| GET | `/medicos` | 200 | — |
| GET | `/medicos/{id}` | 200 | 404 |
| PUT | `/medicos/{id}` | 200 | 400, 404, 409 |
| DELETE | `/medicos/{id}` | 204 | 404, 409 |

### Consultas
| Método | Rota | Sucesso | Erros |
|---|---|---|---|
| POST | `/consultas` | 201 + Location | 400, 404, 409 |
| GET | `/consultas` | 200 | — |
| GET | `/consultas/{id}` | 200 | 404 |
| PUT | `/consultas/{id}` | 200 | 400, 404, 409 |
| DELETE | `/consultas/{id}` | 204 | 404, 409 |

Método não suportado → **405** · erro inesperado → **500** com mensagem genérica.

### Exemplos de JSON

**POST /cidadaos**
```json
{
  "nome": "Rosa Lima",
  "cpf": "40312567899",
  "cartaoSus": "700100000000099",
  "dataNascimento": "1955-06-20",
  "telefone": "11987650099",
  "email": "rosa@email.com",
  "gestante": false,
  "cronico": true
}
```
Resposta 201 (a API calcula `idade` e `prioridade`):
```json
{ "id": 11, "nome": "Rosa Lima", "...": "...", "idade": 71, "prioridade": 1 }
```

**POST /cidadaos/11/consultas** (agendar)
```json
{ "medicoId": 1, "especialidadeId": 1, "dataConsulta": "2026-12-01T09:00:00" }
```

**PUT /consultas/12** (registrar que a consulta foi realizada)
```json
{ "medicoId": 1, "especialidadeId": 1, "dataConsulta": "2026-11-12T08:00:00",
  "status": "REALIZADA", "esperaMin": 12 }
```

**Erro padronizado (ex.: 409 horário ocupado)**
```json
{
  "timestamp": "2026-11-05T14:30:00",
  "status": 409,
  "erro": "Conflict",
  "mensagem": "O medico 1 ja tem uma consulta agendada em 12/11/2026 as 08:00. Escolha outro horario.",
  "caminho": "/cidadaos/11/consultas",
  "campos": null
}
```

## Regras de negócio

1. **Horário ocupado:** um médico não pode ter duas consultas `AGENDADA` no mesmo horário → `HorarioOcupadoException` (409).
2. **Médico inativo** não recebe agendamento (409).
3. **Especialidade:** o médico precisa atender a especialidade escolhida, de acordo com a tabela N:N `TB_MEDICO_ESPECIALIDADE` (409).
4. **Prioridade automática:** calculada pelo domínio (`Cidadao.getPrioridade()`): 1 = idoso 60+ ou gestante, 2 = crônico, 3 = normal.
5. **Consulta finalizada** (`REALIZADA`, `FALTOU` ou `CANCELADA`) não pode mais ser alterada (409).
6. **REALIZADA exige tempo de espera** (`esperaMin`), que alimenta a IA de previsão de espera.
7. **Remoção com filhos:** cidadão com consultas, ou consulta com lembretes, não pode ser removido (409).

## Uso de IA generativa

O código deste projeto foi gerado em grande parte com o Claude (Anthropic), a partir das decisões do grupo. O grupo é responsável por revisar, testar e saber explicar cada parte.
