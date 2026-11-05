# CuidAí — Passo a passo da entrega de Java (DDD)

> Comecem só depois que o `cuidai.sql` tiver rodado sem erro no Oracle, porque a API usa essas tabelas.

## Passo 1 — Rodar a API no computador

1. Instale o **IntelliJ IDEA Community**, se ainda não tiver.
2. Abra a pasta `2-java-api` (File › Open). O IntelliJ reconhece o `pom.xml` e baixa as dependências.
3. Se pedir JDK, escolha **Download JDK › versão 17** (Temurin).
4. Configure as variáveis de ambiente: **Run › Edit Configurations › CuidaiApplication › Environment variables**:
   ```
   DB_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL;DB_USER=rmSEURM;DB_PASSWORD=SUASENHA
   ```
5. Clique em ▶ Run. No console deve aparecer `Tomcat started on port 8080`.
6. Abra no navegador: `http://localhost:8080/cidadaos`. A lista dos 10 cidadãos do script deve aparecer.

## Passo 2 — Testar no Insomnia ou Postman

Esse roteiro também serve para a apresentação:

| # | Requisição | Esperado |
|---|---|---|
| 1 | `GET /cidadaos` | 200, lista |
| 2 | `POST /cidadaos` com o JSON do README | **201** e header `Location` |
| 3 | `POST /cidadaos/{novoId}/consultas` com `{"medicoId":1,"especialidadeId":1,"dataConsulta":"2026-12-01T09:00:00"}` | **201**, prioridade calculada |
| 4 | Repetir o passo 3 com outro cidadão, mesmo médico e mesmo horário | **409** horário ocupado ✅ regra de negócio |
| 5 | `POST /cidadaos` com `"cpf": "123"` e sem nome | **400** com a lista de campos ✅ erro ao vivo |
| 6 | `GET /cidadaos/9999` | **404** ✅ erro ao vivo |
| 7 | `DELETE /cidadaos` (sem id) | **405** |
| 8 | `PUT /consultas/{id}` mudando status para `REALIZADA` com `esperaMin` | 200 |
| 9 | `PUT` de novo na mesma consulta | **409** (consulta já finalizada) |
| 10 | `DELETE /cidadaos/1` | **409** (tem consultas) |
| 11 | `DELETE /cidadaos/{novoId}` de um cidadão sem consultas | **204** |

Tirem prints do Insomnia para a documentação.

## Passo 3 — Subir no GitHub

O Render publica a partir de um repositório do GitHub.

1. Crie um repositório, por exemplo `cuidai-api`.
2. Envie o conteúdo da pasta `2-java-api`. O `.gitignore` já impede o envio da pasta `target`.
3. ⚠️ Confira que **nenhum arquivo tem a senha do banco**. Ela só vai no Render.

## Passo 4 — Publicar no Render

1. Entre em **render.com** e faça login com o GitHub.
2. Clique em **New › Web Service** e escolha o repositório `cuidai-api`.
3. Preencha:
   - **Language/Runtime:** `Docker` (o Render detecta o Dockerfile)
   - **Instance type:** `Free`
4. Em **Environment Variables**, adicione:
   - `DB_URL` = `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL`
   - `DB_USER` = RM de quem rodou o script
   - `DB_PASSWORD` = senha do Oracle
   - `CORS_ORIGINS` = URL do front na Vercel, quando existir (pode deixar sem por enquanto)
5. Clique em **Deploy**. O primeiro build demora uns 5 a 10 minutos.
6. Teste em `https://SEU-APP.onrender.com/cidadaos`.

> ⚠️ **O plano gratuito "dorme" depois de 15 minutos sem uso**, e a primeira chamada leva até 1 minuto. **Abram a URL uns 2 minutos antes da apresentação e antes de o professor corrigir.** A disciplina tira 10 pontos se a API estiver fora do ar.

## Passo 5 — Documentação em PDF (8 seções)

| Seção | O que colocar | De onde tirar |
|---|---|---|
| 1. Capa e links | Integrantes com RM, URL do Render, link do vídeo | — |
| 2. A solução | Problema, quem usa, o que a API resolve | Levantamento do banco |
| 3. Diagrama de classes | `Cidadao`, `Medico`, `Consulta`, `StatusConsulta` com atributos e tipos; Cidadao 1—N Consulta, Medico 1—N Consulta | Pacote `model` (dá para gerar no IntelliJ: botão direito no pacote › Diagrams) |
| 4. Modelo do banco | DER com PK e a FK do 1:N | Modelo relacional do Data Modeler |
| 5. Endpoints | Método, rota, status, JSON de entrada e saída | Tabelas e exemplos do README |
| 6. Estrutura | Pacotes e a responsabilidade de cada um | Seção "Estrutura" do README |
| 7. Regras e validações | Regras de negócio e validações de cada DTO | Seção "Regras" do README e anotações dos DTOs |
| 8. Deploy | Docker multi-stage, Render e **os nomes** das variáveis (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `CORS_ORIGINS`), **sem os valores** | Passo 4 |

## Passo 6 — Vídeo (até 5 minutos, YouTube não listado)

O vídeo mostra o **front funcionando integrado à API** e explica o sistema. Ele fica para depois que fizermos o front (matéria 3).

## Passo 7 — .zip da entrega

```
GS_DDD_CuidAi.zip
├── pom.xml
├── Dockerfile
├── .dockerignore
├── src/            (projeto completo, SEM a pasta target)
├── sql/cuidai.sql
├── Documentacao_CuidAi_API.pdf
└── README.md       (integrantes, RM, como rodar, URL do Render, link do vídeo)
```

## Checklist final

- [ ] Nenhum usuário ou senha no código ou no README
- [ ] O .zip não tem a pasta `target`
- [ ] A URL do Render está funcionando e está no README
- [ ] Os erros voltam em JSON, nunca como página HTML
- [ ] O link do vídeo está no README
- [ ] Todos sabem explicar o caminho controller → DTO → DAO → banco
