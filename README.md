# Pokédex Clean Architecture

Aplicação full stack de Pokédex com **backend Java/Spring Boot** e **frontend React/Vite**. O projeto consome a PokeAPI, mantém cache local em H2, serve imagens pelo próprio backend e oferece uma interface com Pokédex, favoritos, comparação e modo Trunfo.

## Sumário

- [Stack](#stack)
- [Executando com Docker](#executando-com-docker)
- [Executando localmente](#executando-localmente)
- [Build e testes](#build-e-testes)
- [Arquitetura](#arquitetura)
- [Endpoints](#endpoints)
- [Swagger / OpenAPI](#swagger--openapi)
- [Configurações](#configurações)
- [Deploy no Railway](#deploy-no-railway)
- [Cache e dados](#cache-e-dados)
- [Tradução pt-BR](#tradução-pt-br)
- [Graphify](#graphify)
- [Problemas comuns](#problemas-comuns)

## Stack

| Camada | Tecnologias |
| --- | --- |
| Backend | Java 17, Spring Boot 4.1, WebFlux/WebClient, JDBC, H2, Actuator, Lombok |
| Frontend | React 19.2, TypeScript, Vite, lucide-react |
| Infra local | Docker Compose, volumes Docker para H2 e imagens |
| Arquitetura | Clean Architecture, use cases, portas de aplicação e adapters |

## Executando com Docker

Na raiz do projeto:

```bash
docker compose up --build
```

Acesse:

| Serviço | URL |
| --- | --- |
| Frontend | <http://localhost:3000> |
| Backend | <http://localhost:8080> |

O Compose faz build do backend e do frontend porque ambos usam `build:` no `docker-compose.yml`.

## Executando localmente

### Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### Frontend

```powershell
cd frontend
npm install
npm run dev
```

Por padrão, o frontend aponta para `http://localhost:8080`. Para alterar:

```text
VITE_API_BASE_URL=http://localhost:8080
```

## Build e testes

### Backend

```powershell
cd backend
.\mvnw.cmd test
```

### Frontend

```powershell
cd frontend
npm run build
```

O CI em `.github/workflows/build.yml` valida backend e frontend em push e pull request.

## Deploy no Railway

O deploy usa dois services Railway no mesmo projeto:

- backend com root directory `/backend`;
- frontend com root directory `/frontend`.

Siga o passo a passo em [docs/deploy-railway.md](docs/deploy-railway.md).

## Arquitetura

Fluxo principal do backend:

```text
Controllers
  -> Use cases
  -> Portas de aplicação
  -> Adapters JDBC/PokeAPI/storage
  -> H2, PokeAPI e filesystem
```

### Backend

| Pacote | Responsabilidade |
| --- | --- |
| `api/controller` | Endpoints REST separados por catálogo, detalhe, imagem, i18n, Trunfo e manutenção de traduções |
| `application/usecase` | Regras de aplicação e orquestração dos fluxos |
| `application/port` | Contratos consumidos pelos use cases |
| `domain/model` | Modelos compartilhados do domínio |
| `infrastructure/persistence` | Cache JDBC separado em catálogo e detalhe |
| `infrastructure/pokeapi` | Cliente PokeAPI e mappers separados por summary, detail e species |
| `infrastructure/localization` | Tradução pt-BR e localização de textos de species |
| `infrastructure/storage` | Cache físico de imagens |

### Frontend

| Diretório | Responsabilidade |
| --- | --- |
| `app/App.tsx` | Composição global mínima |
| `app/AppChrome.tsx` | Shell visual, navegação e troca de idioma |
| `app/AppRoutes.tsx` | Seleção das views |
| `features/pokemon` | API, tipos, hook `usePokemonExplorer`, listagem e detalhe |
| `features/favorites` | Favoritos salvos em `localStorage` |
| `features/compare` | Comparação de dois Pokémon |
| `features/trunfo` | API, DTO/mapper, tipos, regras, deck e estado do jogo |
| `shared/api` | Configuração da URL base do backend |
| `shared/i18n` | Idioma, mensagens, labels e tradução sob demanda |
| `shared/components` e `shared/utils` | Componentes e helpers reutilizáveis |

O frontend não busca imagens externas diretamente. Ele consome URLs internas servidas pelo backend.

## Endpoints

### Pokémon

```http
GET /api/pokemon?limit=24&offset=0
GET /api/pokemon/search?q=pikachu
GET /api/pokemon/types
GET /api/pokemon/type/{typeName}?limit=24&offset=0
GET /api/pokemon/{nameOrId}
GET /api/pokemon/{pokemonId}/images/{imageType}
```

### Trunfo

```http
GET /api/trunfo/cards?limit=24&offset=0
```

### i18n

```http
POST /api/i18n/translate
```

### Administração de traduções

```http
GET  /api/admin/translations/missing?limit=2000
POST /api/admin/translations/refresh?limit=2000
GET  /api/admin/translations/status
POST /api/admin/translations/cleanup-invalid-cache
```

Endpoints admin exigem:

```http
X-Admin-Token: local-dev-token
```

### Actuator

```http
GET /actuator/health
GET /actuator/metrics
```

## Swagger / OpenAPI

Com o backend em execução, acesse:

| Recurso | URL |
| --- | --- |
| Swagger UI | <http://localhost:8080/swagger-ui.html> |
| OpenAPI JSON | <http://localhost:8080/v3/api-docs> |

A documentação está em pt-BR e cobre:

- descrição geral da API;
- grupos por domínio: Pokémon, imagens, Trunfo, i18n e administração;
- parâmetros de path/query;
- exemplos de entrada e saída;
- schemas dos modelos principais;
- autenticação por `X-Admin-Token` nos endpoints administrativos.

## Configurações

Principais variáveis de ambiente:

| Variável | Padrão | Uso |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | Porta do backend |
| `POKEAPI_BASE_URL` | `https://pokeapi.co/api/v2` | URL da PokeAPI |
| `POKEDEX_BOOTSTRAP_ENABLED` | `true` | Ativa carga inicial |
| `POKEDEX_BOOTSTRAP_LIMIT` | `1025` | Limite de Pokémon no bootstrap |
| `POKEDEX_BOOTSTRAP_DETAILS_ENABLED` | `true` | Carrega detalhes no bootstrap |
| `POKEDEX_IMAGES_STORAGE_PATH` | `./data/pokedex-images` | Diretório de imagens |
| `POKEDEX_ADMIN_ENABLED` | `true` | Ativa endpoints admin |
| `POKEDEX_ADMIN_TOKEN` | `local-dev-token` | Token dos endpoints admin |
| `POKEDEX_CORS_ALLOWED_ORIGINS` | origens locais | Origens HTTP liberadas pelo backend |
| `VITE_API_BASE_URL` | `http://localhost:8080` | URL do backend usada pelo frontend |

## Cache e dados

Scripts de banco:

```text
backend/src/main/resources/db/schema.sql
backend/src/main/resources/db/data.sql
```

Paths usados no Docker:

```text
/data/h2/pokedex-db
/data/pokedex-images
```

Volumes:

```yaml
volumes:
  pokedex-h2-data:
  pokedex-images:
  libretranslate-data:
```

Fluxo de imagem:

```text
PokeAPI
  -> backend baixa a imagem
  -> backend salva em /data/pokedex-images
  -> backend grava metadados em pokemon_image
  -> frontend consome /api/pokemon/{id}/images/{imageType}
```

## Tradução pt-BR

Fluxo:

```text
PokeAPI em inglês
  -> provedor de tradução
  -> pokemon_text_translation
  -> pokemon_species com locale atual
  -> frontend lê o backend
```

Configurações:

```text
POKEDEX_TRANSLATION_ENABLED=true
POKEDEX_TRANSLATION_LIBRETRANSLATE_URL=
POKEDEX_TRANSLATION_URL=https://api.mymemory.translated.net/get
POKEDEX_TRANSLATION_FALLBACK_URL=https://translate.googleapis.com
POKEDEX_TRANSLATION_TIMEOUT_SECONDS=8
```

O serviço `libretranslate` existe no Compose com profile opcional:

```bash
docker compose --profile optional-translation up --build
```

Se a tradução falhar, o backend mantém o item pendente para nova tentativa.

## Graphify

As saídas do Graphify são geradas em `graphify-out/` e não devem ser versionadas.

Atualizar o grafo:

```powershell
& "$(Get-Content graphify-out/.graphify_python)" -m graphify . --update
```

Recalcular comunidades:

```powershell
& "$(Get-Content graphify-out/.graphify_python)" -m graphify cluster-only C:\workspace\projetos_novos\pokedex-clean
```

Atualizar labels com Gemini:

```powershell
& "$(Get-Content graphify-out/.graphify_python)" -m graphify label --backend gemini
```

Relatórios gerados localmente:

```text
graphify-out/GRAPH_REPORT.md
graphify-out/graph.html
graphify-out/graph.json
```

## Problemas comuns

### Docker Desktop desligado

Abra o Docker Desktop antes de rodar:

```bash
docker compose up --build
```

### Portas ocupadas

Libere as portas `3000` e `8080`, ou ajuste o mapeamento no `docker-compose.yml`.

### Resetar banco e imagens

```powershell
docker compose down -v
docker compose up --build
```

Esse comando remove os volumes `pokedex-h2-data`, `pokedex-images` e `libretranslate-data`.

### Desativar bootstrap inicial

```text
POKEDEX_BOOTSTRAP_ENABLED=false
```

### Reduzir carga inicial

```text
POKEDEX_BOOTSTRAP_LIMIT=151
POKEDEX_BOOTSTRAP_DETAILS_ENABLED=false
```
