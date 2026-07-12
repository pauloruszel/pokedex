# Pokedex Clean Architecture

Pokedex full stack com backend Java/Spring Boot e frontend React/Vite. O projeto consome a PokeAPI, mantém cache local em H2, serve imagens pelo backend e oferece uma UI com Pokedex, favoritos, comparação e modo Trunfo.

## Stack

- Backend: Java 17, Spring Boot 4.1, WebFlux/WebClient, JDBC, H2, Actuator e Lombok.
- Frontend: React 19.2, TypeScript, Vite e `lucide-react`.
- Infra local: Docker Compose, volume H2 e volume de imagens.
- Arquitetura: controllers finos, use cases por fluxo, portas de aplicação e adapters de infraestrutura.

## Como Rodar

Com Docker, na raiz:

```bash
docker compose up --build
```

Acesse:

```text
Frontend: http://localhost:3000
Backend:  http://localhost:8080
```

Localmente:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

```powershell
cd frontend
npm install
npm run dev
```

Por padrão o frontend usa `http://localhost:8080`. Para alterar:

```text
VITE_API_BASE_URL=http://localhost:8080
```

## Build e Testes

Backend:

```powershell
cd backend
.\mvnw.cmd test
```

Frontend:

```powershell
cd frontend
npm run build
```

O CI em `.github/workflows/build.yml` valida backend e frontend em push e pull request.

## Backend

Fluxo principal:

```text
Controllers
  -> use cases
  -> portas de aplicação
  -> adapters JDBC/PokeAPI/storage
  -> H2, PokeAPI e filesystem
```

Módulos principais:

- `api/controller`: endpoints REST separados por catálogo, detalhe, imagem, i18n, Trunfo e manutenção de traduções.
- `application/usecase`: regras de aplicação e orquestração dos fluxos.
- `application/port`: contratos consumidos pelos use cases.
- `domain/model`: modelos compartilhados do domínio.
- `infrastructure/persistence`: cache JDBC separado em catálogo e detalhe.
- `infrastructure/pokeapi`: cliente PokeAPI e mappers separados por summary, detail e species.
- `infrastructure/localization`: tradução pt-BR e localização de species.
- `infrastructure/storage`: cache físico de imagens.

### Endpoints

Pokemon:

```text
GET /api/pokemon?limit=24&offset=0
GET /api/pokemon/search?q=pikachu
GET /api/pokemon/types
GET /api/pokemon/type/{typeName}?limit=24&offset=0
GET /api/pokemon/{nameOrId}
GET /api/pokemon/{pokemonId}/images/{imageType}
```

Trunfo:

```text
GET /api/trunfo/cards?limit=24&offset=0
```

i18n:

```text
POST /api/i18n/translate
```

Admin de traduções:

```text
GET  /api/admin/translations/missing?limit=2000
POST /api/admin/translations/refresh?limit=2000
GET  /api/admin/translations/status
POST /api/admin/translations/cleanup-invalid-cache
```

Actuator:

```text
GET /actuator/health
GET /actuator/metrics
```

Endpoints admin exigem:

```text
X-Admin-Token: local-dev-token
```

Configure outro token com:

```text
POKEDEX_ADMIN_TOKEN=um-token-local
```

## Frontend

Estrutura atual:

- `app/App.tsx`: composição global mínima.
- `app/AppChrome.tsx`: shell visual, navegação e troca de idioma.
- `app/AppRoutes.tsx`: seleção das views.
- `features/pokemon`: API, tipos, hook `usePokemonExplorer`, Pokedex e componentes de detalhe/lista.
- `features/favorites`: favoritos salvos em `localStorage`.
- `features/compare`: comparação de dois Pokemon.
- `features/trunfo`: API, DTO/mapper, tipos, regras, deck e estado do jogo.
- `shared/api/apiConfig.ts`: base URL do backend.
- `shared/i18n`: contrato de idioma, mensagens, labels e tradução sob demanda.
- `shared/components` e `shared/utils`: componentes e helpers pequenos reutilizáveis.

O frontend não busca imagens externas diretamente. Ele recebe URLs internas do backend.

## Cache e Dados

Scripts de banco:

```text
backend/src/main/resources/db/schema.sql
backend/src/main/resources/db/data.sql
```

Banco H2 em Docker:

```text
/data/h2/pokedex-db
```

Imagens em Docker:

```text
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

Configurações principais:

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

O projeto mantém saídas do Graphify em `graphify-out/` para acompanhar acoplamento e coesão entre módulos.

Atualizar o grafo:

```powershell
graphify . --update
graphify label --backend gemini
```

Relatórios principais:

```text
graphify-out/GRAPH_REPORT.md
graphify-out/graph.html
graphify-out/graph.json
```

## Problemas Comuns

Docker Desktop desligado: abra o Docker Desktop antes de `docker compose up --build`.

Porta ocupada: libere `3000` para o frontend ou `8080` para o backend, ou ajuste as portas no Compose.

Resetar banco e imagens:

```powershell
docker compose down -v
docker compose up --build
```

Desativar bootstrap inicial:

```text
POKEDEX_BOOTSTRAP_ENABLED=false
```

Reduzir carga inicial:

```text
POKEDEX_BOOTSTRAP_LIMIT=151
POKEDEX_BOOTSTRAP_DETAILS_ENABLED=false
```
