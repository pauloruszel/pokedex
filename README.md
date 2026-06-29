# Pokédex Clean Architecture — Java + React

Projeto dividido em dois serviços independentes:

- `backend/`: Java 17, Spring Boot 4.1, Lombok, H2, WebFlux/WebClient e Clean Architecture.
- `frontend/`: React 19.2, Vite, TypeScript e interface premium de Pokédex Lab.

## Arquitetura geral

```text
frontend React
  -> consome somente o backend Java

backend Java
  -> consulta PokéAPI
  -> salva dados no H2 persistente
  -> baixa/cacheia imagens em volume Docker
  -> expõe endpoints próprios para dados e imagens

Docker
  -> volume para banco H2
  -> volume para imagens cacheadas
```

## Backend

Principais responsabilidades:

- buscar Pokémon na PokéAPI;
- persistir dados no H2;
- executar `schema.sql` e `data.sql` separados;
- baixar imagens da origem da PokéAPI e salvar em volume Docker;
- gravar metadados das imagens no banco;
- servir imagens pelo próprio backend.

Endpoints principais:

```text
GET /api/pokemon?limit=24&offset=0
GET /api/pokemon/{nameOrId}
GET /api/pokemon/search?q=pikachu
GET /api/pokemon/types
GET /api/pokemon/type/{typeName}?limit=24&offset=0
GET /api/pokemon/{pokemonId}/images/official-artwork
GET /api/pokemon/{pokemonId}/images/front-default
```

## H2

Scripts separados:

```text
backend/src/main/resources/db/schema.sql
backend/src/main/resources/db/data.sql
```

O banco usa modo arquivo:

```text
/data/h2/pokedex-db
```

## Imagens

O frontend **não acessa imagens externas diretamente**.

Fluxo:

```text
PokéAPI/PokeAPI sprites
  -> backend baixa a imagem
  -> backend salva em /data/pokedex-images
  -> backend grava metadados em pokemon_image
  -> frontend consome /api/pokemon/{id}/images/official-artwork
```

## Frontend Premium UI

Implementações de UX/UI incluídas:

- visual “Pokédex Lab” com dark mode premium;
- hero tecnológico com glassmorphism;
- grid de cards com cor baseada no tipo do Pokémon;
- imagem grande com efeito de profundidade;
- busca por nome ou número;
- filtros rápidos por tipo;
- favoritos locais usando `localStorage`;
- comparador de dois Pokémon;
- drawer lateral de detalhe completo;
- barras animadas de status;
- galeria com official artwork e sprite;
- linha evolutiva;
- skeleton loading;
- layout responsivo para desktop e mobile.

## Rodando com Docker

Na raiz do projeto:

```bash
docker compose up --build
```

Acesse:

```text
Frontend: http://localhost:3000
Backend:  http://localhost:8080
```

## Volumes Docker

```yaml
volumes:
  pokedex-h2-data:
  pokedex-images:
```

| Volume | Função |
|---|---|
| `pokedex-h2-data` | preserva banco H2 |
| `pokedex-images` | preserva imagens cacheadas |

## Rodando localmente sem Docker

Backend:

```bash
cd backend
./mvnw spring-boot:run
```

No Windows:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

## Build e testes

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

O repositório também possui CI em `.github/workflows/build.yml` para validar backend e frontend em push e pull request.

## Problemas comuns

### Docker Desktop desligado

Se `docker compose up --build` falhar antes de subir os serviços, confirme se o Docker Desktop está aberto e com o engine ativo.

### Porta 8080 ocupada

Verifique qual processo está usando a porta e encerre-o, ou altere a porta do backend nas configurações do Spring/Docker Compose.

### Resetar banco H2

Para limpar os dados persistidos no Docker:

```powershell
docker compose down -v
docker compose up --build
```

Esse comando remove os volumes `pokedex-h2-data` e `pokedex-images`.

### Limpar cache de imagens

O cache de imagens fica no volume `pokedex-images`. Para recriar tudo, remova os volumes com `docker compose down -v` e suba novamente.

### Desativar bootstrap inicial

Use a propriedade:

```text
pokedex.bootstrap.enabled=false
```

No Docker, defina a variável equivalente no serviço do backend.

### Forçar recarga dos textos pt-BR

Os textos localizados ficam em `pokemon_text_translation` e `pokemon_species`.

O backend usa uma etapa de preenchimento em segundo plano:

```text
PokeAPI em inglês
  -> provedor de tradução en|pt-BR
  -> pokemon_text_translation
  -> pokemon_species com text_locale atual
  -> frontend lê somente o backend
```

Configurações:

```text
POKEDEX_BOOTSTRAP_DETAILS_ENABLED=true
POKEDEX_TRANSLATION_ENABLED=true
POKEDEX_TRANSLATION_LIBRETRANSLATE_URL=http://libretranslate:5000
POKEDEX_TRANSLATION_URL=https://api.mymemory.translated.net/get
POKEDEX_TRANSLATION_FALLBACK_URL=https://translate.googleapis.com
POKEDEX_TRANSLATION_TIMEOUT_SECONDS=8
```

O Docker Compose sobe um serviço `libretranslate` para tradução local/self-hosted. Se ele não responder, o backend tenta os provedores externos configurados como fallback. Para forçar recarga, limpe as linhas de `pokemon_species` e `pokemon_text_translation`, ou altere a versão `CACHE_LOCALE` no código.

Se o provedor falhar, o backend não salva texto inglês como pt-BR; a tradução fica pendente e será tentada novamente em uma próxima execução.

Endpoints operacionais locais:

```text
GET  /api/admin/translations/missing?limit=2000
POST /api/admin/translations/refresh?limit=2000
GET  /api/admin/translations/status
```

Use o primeiro para listar Pokémon sem descrição pt-BR no cache atual. Use o segundo para buscar o texto em inglês na PokeAPI, traduzir, salvar em `pokemon_text_translation`/`pokemon_species` e devolver o que foi atualizado ou ainda falhou. Use o terceiro para acompanhar o status persistido do job.

Esses endpoints são protegidos por token:

```text
X-Admin-Token: local-dev-token
```

No Docker, personalize com:

```text
POKEDEX_ADMIN_TOKEN=um-token-local
```

### Healthcheck e métricas

O backend expõe:

```text
GET /actuator/health
GET /actuator/metrics
```

O Docker Compose usa esses endpoints para esperar o backend estar saudável antes de disponibilizar o frontend.

### Frontend parece travado ao abrir dossiê

O frontend abre primeiro uma prévia do card e carrega detalhes em seguida. O backend responde com URLs internas de imagem e baixa o arquivo em segundo plano, então a primeira abertura pode mostrar o estado de carregamento por alguns instantes.
