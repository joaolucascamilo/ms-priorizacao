# ms-priorizacao

Microserviço responsável pela **priorização automática de ocorrências** de infraestrutura urbana. Faz parte de um sistema distribuído para gestão inteligente de ocorrências municipais (TCC).

---

## Visão Geral

O serviço recebe dados de uma ocorrência e calcula uma pontuação composta com base em quatro critérios ponderados, atribuindo um nível de prioridade que orienta as equipes de fiscalização na tomada de decisão.

### Critérios de Pontuação

| Critério | Pontos máximos | Descrição |
|---|---|---|
| Tipo da ocorrência | 40 pts | ESGOTO > BURACO > SEMAFORO > LAMPADA |
| Quantidade de denúncias | 20 pts | 10+ denúncias = 20 pts |
| Tempo sem solução | 20 pts | 30+ dias = 20 pts |
| Proximidade a locais críticos | 20 pts | Escolas/hospitais em até 200m = 20 pts |

### Níveis de Prioridade

| Nível | Pontuação |
|---|---|
| `CRITICA` | ≥ 80 |
| `ALTA` | ≥ 50 |
| `MEDIA` | ≥ 25 |
| `BAIXA` | < 25 |

---

## Tecnologias

- **Java 17**
- **Spring Boot 3.3.6**
- **Spring Cloud 2025.0.2** (OpenFeign)
- **Spring Data JPA** + **PostgreSQL**
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
  "id": 1,
  "tipoOcorrencia": "ESGOTO",
  "quantidadeDenuncias": 5,
  "dataCriacao": "2026-05-01T10:00:00",
  "latitude": -3.7318,
  "longitude": -38.5001
}
```

**Resposta:**
```json
{
  "ocorrenciaId": 1,
  "nivelPrioridade": "ALTA",
  "scoreCalculado": 65,
  "justificativa": "..."
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
├── controller/         # Endpoints REST
├── service/            # Algoritmo de priorização
├── model/              # Entidade JPA (PontuacaoOcorrencia)
├── dto/                # Objetos de entrada e saída da API
├── enums/              # NivelPrioridade
├── repository/         # Acesso ao banco via Spring Data JPA
├── client/             # Feign client (integração futura com ms-geo)
└── config/             # Configuração do Feign
```

---

## Integração com Outros Serviços

| Serviço | Comunicação | Status |
|---|---|---|
| `ms-geo` | OpenFeign (REST) | Preparado — `GeoClient` implementado como stub |

A integração com o `ms-geo` está preparada para buscar locais críticos (escolas e hospitais) dinamicamente. Atualmente, o cálculo de proximidade usa coordenadas fixas via fórmula de Haversine.

---

## Verificação de Saúde

```
GET http://localhost:8085/actuator/health
```
