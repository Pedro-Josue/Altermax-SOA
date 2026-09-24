# Altermax

API de inteligência competitiva automotiva que compara um veículo Ford armazenado localmente com uma variante concorrente consultada no fornecedor Cars-Data. O resultado usa um schema comum, normaliza unidades, explicita dados ausentes e fica registrado no histórico da execução.

## Visão funcional

1. O cliente autentica-se em `POST /api/auth/login`.
2. Seleciona um Ford de `GET /api/ford-vehicles`.
3. Pesquisa uma variante em `GET /api/competitors/search?q=...`.
4. Escolhe o `externalId` da variante concorrente desejada.
5. Cria a comparação em `POST /api/comparisons`, informando os atributos desejados.
6. Consulta o snapshot em `/api/comparison-history`.

## Arquitetura

O Altermax é um **monólito modular** 

<img width="1406" height="276" alt="diagram-1790263041840" src="https://github.com/user-attachments/assets/b17faef1-8ece-4335-969c-06277f9cac47" />

### Módulos e dependências

| Módulo | Responsabilidade | Fronteira pública principal |
|---|---|---|
| `auth` | usuários, roles, senha BCrypt, login, emissão/validação JWT e Spring Security | endpoint de login e identidade autenticada |
| `fordcatalog` | entidades e especificações Ford, seed, consultas e CRUD ADMIN | `FordVehicleCatalog` |
| `competitor` | `RestClient`, timeouts, API key, JSON externo, mapeamento e tradução de falhas | `CompetitorCatalog` |
| `comparison` | atributos, validação, alinhamento, unidades e orquestração | recurso `/api/comparisons` |
| `history` | persistência de usuário, referências, atributos e snapshot JSON | `ComparisonHistoryRecorder` |
| `shared` | formato de erro e configuração OpenAPI | elementos transversais mínimos |

Os repositories são package-private e permanecem no módulo proprietário. Controllers nunca expõem entidades JPA. `comparison` conhece apenas contratos dos outros módulos; `competitor` é o único que conhece URL, header e formato da Cars-Data.

## Segurança e JWT

<img width="2360" height="1292" alt="diagrama-jwt" src="https://github.com/user-attachments/assets/731387a6-a058-4abc-aebb-ce6bc81bf770" />

Tokens são assinados com HMAC, têm uma hora de validade, carregam apenas `sub`, `roles`, `iat` e `exp`, e nunca incluem senha. O filtro rejeita token ausente, adulterado ou expirado com `401`; usuário autenticado sem role recebe `403`.

| Usuário de demonstração | Senha | Roles |
|---|---|---|
| `user` | `user123` | `ROLE_USER` |
| `admin` | `admin123` | `ROLE_USER`, `ROLE_ADMIN` |

`ROLE_USER` consulta veículos Ford e concorrentes, cria comparações e consulta o próprio histórico. `ROLE_ADMIN` também cadastra, substitui e remove Ford; ao consultar histórico, vê todos os registros.

## Fluxo de comparação

<img width="2760" height="1434" alt="fluxo-comp" src="https://github.com/user-attachments/assets/df95da9e-b995-40c0-a41f-7ec0dac41001" />

## Tecnologias e pré-requisitos

- Java 21 (o `pom.xml` compila com `release 21`)
- Maven 3.9+
- Spring Boot 3.5, Web, Data JPA, Security e Validation
- JJWT, H2 em memória, Springdoc OpenAPI
- JUnit 5, Mockito, MockMvc e Spring Security Test

## Configuração da Cars-Data

Se a equipe tiver acesso provisionado à API Cars-Data, exponha a chave apenas no ambiente:

```powershell
$env:CARSDATA_API_KEY = "sua-chave"
```

Linux/macOS:

```bash
export CARSDATA_API_KEY="sua-chave"
```

A aplicação envia a chave no header `X-Api-Key`. A URL padrão é `https://api.cars-data.com/v1`; timeouts são 3 s para conexão e 8 s para leitura. Sem chave, endpoints externos retornam `503`, enquanto login, catálogo Ford e Swagger continuam disponíveis.

## Executar

```bash
mvn spring-boot:run
```

Ou:

```bash
mvn clean package
java -jar target/altermax-1.0.0.jar
```

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`
- H2 Console: `http://localhost:8081/h2-console`
- JDBC URL: `jdbc:h2:mem:altermax`
- usuário H2: `sa`; senha vazia

O H2 é exclusivamente em memória. Usuários, roles, catálogo e histórico são perdidos quando a aplicação encerra e recriados (exceto histórico) na próxima inicialização.

## Endpoints REST

| Método | Recurso | Acesso | Resposta principal |
|---|---|---|---|
| POST | `/api/auth/login` | público | `200` |
| GET | `/api/ford-vehicles` e `/{id}` | USER/ADMIN | `200`, `404` |
| POST | `/api/ford-vehicles` | ADMIN | `201` + `Location` |
| PUT | `/api/ford-vehicles/{id}` | ADMIN | `200` |
| DELETE | `/api/ford-vehicles/{id}` | ADMIN | `204` |
| GET | `/api/competitors/search?q=` | USER/ADMIN | `200`, `429`, `502`, `503` |
| GET | `/api/competitors/{externalId}` | USER/ADMIN | `200`, `404`, `502`, `503` |
| POST | `/api/comparisons` | USER/ADMIN | `201` + `Location`, `422` |
| GET | `/api/comparison-history` e `/{id}` | USER/ADMIN | `200`, `404` |

### Exemplo de login

Usuário comum (`ROLE_USER`):

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}'
```

Administrador (`ROLE_USER` e `ROLE_ADMIN`):

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Resposta abreviada do usuário comum:

```json
{"accessToken":"eyJ...","tokenType":"Bearer","expiresIn":3600,"username":"user","roles":["ROLE_USER"]}
```

Resposta abreviada do administrador:

```json
{"accessToken":"eyJ...","tokenType":"Bearer","expiresIn":3600,"username":"admin","roles":["ROLE_USER","ROLE_ADMIN"]}
```

### Como realizar uma comparação

Antes de criar a comparação, obtenha os três dados do corpo da requisição:

- `fordVehicleId`: ID de um Ford retornado por `GET /api/ford-vehicles`;
- `competitorExternalId`: valor de `externalId` da variante escolhida em `GET /api/competitors/search?q=...`;
- `attributes`: lista não vazia das chaves que devem ser comparadas.

Envie esses dados para `POST /api/comparisons`. O endpoint exige um JWT no header `Authorization: Bearer SEU_TOKEN` e um corpo JSON com `Content-Type: application/json`:

```bash
curl -X POST http://localhost:8081/api/comparisons \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fordVehicleId": 1,
    "competitorExternalId": "103927",
    "attributes": [
      "power_hp",
      "torque_nm",
      "transmission",
      "fuel_type"
    ]
  }'
```

O processamento:

1. valida as chaves solicitadas;
2. carrega o Ford do catálogo local;
3. consulta os detalhes do concorrente pelo `competitorExternalId`;
4. seleciona somente os atributos solicitados;
5. normaliza valores e unidades compatíveis;
6. alinha Ford e concorrente no mesmo schema e com o mesmo conjunto de chaves;
7. mantém explicitamente os dados ausentes;
8. grava o snapshot no histórico;
9. responde com `201 Created` e o header `Location` apontando para o histórico criado.

Resposta `201 Created` abreviada:

```json
{
  "comparisonId": 1,
  "createdAt": "2026-09-17T18:30:00Z",
  "attributes": ["power_hp", "torque_nm", "transmission", "fuel_type"],
  "vehicles": [
    {"source":"LOCAL_FORD_CATALOG","brand":"Ford","model":"Ranger","variant":"Limited 3.0 V6 Diesel","specifications":{"power_hp":{"value":250,"unit":"hp","available":true}}},
    {"source":"CARS_DATA_API","brand":"Toyota","model":"Hilux","variant":"...","specifications":{"power_hp":{"value":null,"unit":"hp","available":false}}}
  ]
}
```

#### O que pode ser comparado

Qualquer subconjunto não vazio das 18 chaves abaixo pode ser enviado no array `attributes`:

| Chave enviada em `attributes` | Significado | Unidade normalizada |
|---|---|---|
| `power_hp` | potência | `hp` |
| `torque_nm` | torque | `Nm` |
| `engine_displacement_cc` | cilindrada do motor | `cc` |
| `fuel_type` | tipo de combustível | textual |
| `transmission` | transmissão | textual |
| `drivetrain` | tipo de tração | textual |
| `top_speed_kmh` | velocidade máxima | `km/h` |
| `acceleration_0_100_s` | aceleração de 0 a 100 km/h | `s` |
| `fuel_consumption_l_100km` | consumo de combustível | `L/100km` |
| `co2_g_km` | emissão de CO₂ | `g/km` |
| `length_mm` | comprimento | `mm` |
| `width_mm` | largura | `mm` |
| `height_mm` | altura | `mm` |
| `wheelbase_mm` | distância entre-eixos | `mm` |
| `curb_weight_kg` | peso em ordem de marcha | `kg` |
| `cargo_capacity_l` | capacidade de carga/porta-malas | `L` |
| `seats` | quantidade de assentos | sem unidade |
| `price` | preço informado pela fonte | unidade/moeda da fonte |

Chaves desconhecidas retornam `422 ATTRIBUTE_NOT_SUPPORTED`, e atributos repetidos não são aceitos. Ford e concorrente sempre aparecem com o mesmo conjunto de chaves solicitado. Unidades compatíveis são convertidas para a unidade normalizada da tabela; no caso de `price`, a unidade ou moeda fornecida pela fonte é preservada quando disponível.

Se uma fonte não informar um atributo, a aplicação não estima nem inventa a especificação: a chave permanece na resposta com `value: null` e `available: false`, por exemplo:

```json
{
  "value": null,
  "unit": "Nm",
  "available": false
}
```

## Erros

Um `@RestControllerAdvice` e handlers da cadeia Spring Security produzem o mesmo formato, sem stacktrace ou corpo bruto do provedor:

```json
{
  "timestamp": "2026-09-17T18:40:00Z",
  "status": 404,
  "error": "Not Found",
  "code": "FORD_VEHICLE_NOT_FOUND",
  "message": "Veiculo Ford 99 nao encontrado.",
  "path": "/api/ford-vehicles/99",
  "details": []
}
```

São tratados validação (`400`), autenticação (`401`), autorização (`403`), ausência (`404`), método incompatível (`405`), conflito (`409`), mídia não suportada (`415`), atributo não suportado (`422`), rate limit (`429`), resposta inválida (`502`), timeout/indisponibilidade (`503`) e falha interna sanitizada (`500`).

## Testes

```bash
mvn test
mvn clean verify
```

Os testes unitários cobrem JWT, assinatura adulterada, expiração, claims, normalização, mapeamento externo, tradução de falhas e orquestração. Os testes de integração usam Spring Boot, MockMvc, Spring Security e H2 real para login, token ausente/inválido/expirado, autorização, CRUD, comparação, histórico, semântica HTTP, erros e OpenAPI. 
