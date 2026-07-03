# ms-priorizacao

Microserviço responsável pela **priorização automática de ocorrências** de infraestrutura urbana. Faz parte de um sistema distribuído para gestão inteligente de ocorrências municipais (TCC).

---

## Visão Geral

O serviço recebe dados de uma ocorrência e calcula uma pontuação composta com base em três critérios ponderados, atribuindo um nível de prioridade que orienta as equipes de fiscalização na tomada de decisão.

### Critérios de Pontuação

Pontuação máxima total: **80 pts**.

| Critério | Pontos máximos | Regra |
|---|---|---|
| Tipo da ocorrência | 40 pts | `ESGOTO`=40, `BURACO`=35, `SEMAFORO`=30, `LAMPADA`=15, outro=10 |
| Quantidade de denúncias | 20 pts | ≥10 denúncias = 20, 5–9 = 10, 2–4 = 5, <2 = 0 |
| Tempo sem solução | 20 pts | ≥30 dias = 20, 14–29 dias = 10, 7–13 dias = 5, <7 = 0 |

> O critério de proximidade a locais críticos (escolas/hospitais) foi removido do algoritmo. Os campos `latitude` e `longitude` ainda existem no DTO de entrada, mas **não influenciam mais o cálculo do score**.

### Níveis de Prioridade

| Nível | Pontuação |
|---|---|
| `CRITICA` | ≥ 65 |
| `ALTA` | 40 – 64 |
| `MEDIA` | 20 – 39 |
| `BAIXA` | < 20 |

---

## Tecnologias

- **Java 17**
- **Spring Boot 3.3.6**
- **Spring Cloud 2023.0.3** (OpenFeign — dependência declarada no `pom.xml`, mas ainda não utilizada por nenhum client no código)
- **Spring Data JPA** + **PostgreSQL**
- **springdoc-openapi 2.6.0** (Swagger UI / OpenAPI)
- **Spring Boot Actuator**
- **Bean Validation** (`spring-boot-starter-validation`)
- **Maven**
- **Lombok**

---

## Pré-requisitos

- Java 17+
- Maven 3.6+ (ou usar o wrapper `mvnw`)
- PostgreSQL 12+ rodando em `localhost:5432`

---

## Configuração

As configurações estão em `src/main/resources/application.yml`. Antes de subir a aplicação, certifique-se de que o banco existe:

```sql
CREATE DATABASE infra_urbana_priorizacao;
```

O schema da tabela `pontuacao_ocorrencia` é criado automaticamente pelo Hibernate na primeira execução (`ddl-auto: update`).

Variáveis de conexão padrão:

| Propriedade | Valor padrão |
|---|---|
| Porta da aplicação | `8085` |
| URL do banco | `jdbc:postgresql://localhost:5432/infra_urbana_priorizacao` |
| Usuário | `admin` |
| Senha | `password123` |

### CORS

As origens abaixo estão liberadas em `CorsConfig` para todos os endpoints (`/**`):

- `http://localhost:5500`
- `http://127.0.0.1:5500`
- `https://somar.up.railway.app`

Métodos permitidos: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`. Todos os headers são aceitos e `allowCredentials` está habilitado.

---

## Build e Execução

```bash
# Compilar e empacotar
./mvnw clean package

# Executar via plugin Maven
./mvnw spring-boot:run

# Executar o JAR gerado
java -jar target/ms-priorizacao-0.0.1-SNAPSHOT.jar
```

---

## Endpoints da API

Base URL: `http://localhost:8085`

### Calcular prioridade

```
POST /api/priorizacao/calcular
```

**Body:**
```json
{
  "id": 42,
  "tipoOcorrencia": "BURACO",
  "quantidadeDenuncias": 7,
  "dataCriacao": "2026-06-21T10:30:00",
  "latitude": -23.5505,
  "longitude": -46.6333
}
```

> `latitude` e `longitude` são aceitos por compatibilidade, mas não são usados no cálculo do score.

**Resposta:**
```json
{
  "ocorrenciaId": 42,
  "nivelPrioridade": "ALTA",
  "scoreCalculado": 50,
  "justificativa": "Tipo 'BURACO': +35pts. Denúncias (7): +10pts. Tempo sem solução: +5pts. "
}
```

---

### Recalcular prioridade

```
PUT /api/priorizacao/recalcular/{ocorrenciaId}
```

Atualiza o cálculo de uma ocorrência já existente. Registra escaladas de prioridade no log.

---

### Ranking de ocorrências

```
GET /api/priorizacao/ranking
GET /api/priorizacao/ranking?nivel=CRITICA
```

Retorna todas as ocorrências ordenadas por score (decrescente). O parâmetro `nivel` filtra por nível de prioridade.

---

## Estrutura do Projeto

```
src/main/java/com/fiscalizacao/ms_priorizacao/
├── controller/         # Endpoints REST (PriorizacaoController)
├── service/            # Algoritmo de priorização (PriorizacaoService)
├── model/              # Entidade JPA (PontuacaoOcorrencia)
├── dto/                # Objetos de entrada e saída da API
├── enums/              # NivelPrioridade
├── repository/         # Acesso ao banco via Spring Data JPA
└── config/             # CorsConfig e OpenApiConfig (Swagger)
```

---

## Integração com Outros Serviços

| Serviço | Comunicação | Status |
|---|---|---|
| `ms-geo` | OpenFeign (REST) | Não implementado — a dependência `spring-cloud-starter-openfeign` está no `pom.xml` e existe uma propriedade `ms-geo.url` em `application.yml`, mas não há nenhum `@FeignClient`/`GeoClient` no código-fonte |

O critério de proximidade a locais críticos (escolas/hospitais) foi removido do algoritmo de pontuação, então essa integração não é usada atualmente.

---

## Documentação Interativa (Swagger)

Com a aplicação em execução, a documentação OpenAPI fica disponível em:

- Swagger UI: `http://localhost:8085/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8085/v3/api-docs`

---

## Verificação de Saúde

```
GET http://localhost:8085/actuator/health
```
