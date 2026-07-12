# Graph Report - .  (2026-07-11)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 832 nodes · 1539 edges · 76 communities (34 shown, 42 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 97 edges (avg confidence: 0.81)
- Token cost: 1,942 input · 837 output

## Graph Freshness
- Built from commit: `8f4c415f`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Translation Service
- Pokemon View Components
- Pokemon API Controller
- Trunfo Game Logic
- Pokemon Catalog Repository
- Pokemon Image Cache
- Translation Maintenance
- Admin Security Filter
- Image Storage Repository
- PokeApi Data Mappers
- Translation Job Status
- Frontend Dependencies
- Pokemon Localization UseCase
- Trunfo Card Controller
- Global Error Handling
- TypeScript Configuration
- Pokemon Detail Repository
- Species Text Localization
- External Translation Gateways
- System Infrastructure
- Image Cache Tests
- PokeApi Web Client
- API Integration Tests
- App Layout Routing
- WebClient Configuration
- String Formatting Utils
- Background Task Config
- CORS Configuration
- Pokemon Type Definitions
- Application Entry Point
- UI Theme Mapping
- CI Build Workflow
- Maven Wrapper
- HTTP Get Mapping
- HTTP Post Mapping
- Request Path Mapping
- REST Controller Annotation
- Server Web Exchange
- Web Filter Chain
- Task Executor Service
- Pokemon Detail Model
- Reactive Mono Type
- Constructor Injection
- JDBC Database Client
- Portuguese Translation Gateway
- Lombok Required Args
- Database Access Client
- Dependency Injection Helper
- SQL Query Client
- Persistence Layer Client
- Pokemon Detail Entity
- Pokemon Pagination Model
- Pokemon Species Model
- Pokemon Stats Model
- Pokemon Summary Model
- Data Repository Annotation
- Boilerplate Constructor
- Database Transaction Management
- Spring Component Annotation
- Jackson JSON Node
- Detail Data Object
- Species Data Object
- Stat Data Object
- Summary Data Object
- Lombok Constructor
- Reactive Flux Type
- Reactive Stream Mono
- Method Override Annotation
- Actuator Health Config
- Frontend Entry HTML
- Project Maven Artifact

## God Nodes (most connected - your core abstractions)
1. `PokeApiPokemonRepository` - 23 edges
2. `useMessages()` - 20 edges
3. `TranslationCacheService` - 20 edges
4. `PokedexBootstrapRunner` - 18 edges
5. `JdbcPokemonDetailCacheRepository` - 18 edges
6. `TranslationMaintenanceService` - 17 edges
7. `PokemonImage` - 16 edges
8. `compilerOptions` - 16 edges
9. `TranslationKeyNormalizer` - 16 edges
10. `PokemonImageCacheService` - 15 edges

## Surprising Connections (you probably didn't know these)
- `H2 Persistence` --semantically_similar_to--> `Datasource Configuration`  [INFERRED] [semantically similar]
  README.md → backend/src/main/resources/application.yml
- `Image Cache Flow` --semantically_similar_to--> `Images Storage Configuration`  [INFERRED] [semantically similar]
  README.md → backend/src/main/resources/application.yml
- `Translation Flow` --semantically_similar_to--> `Translation Configuration`  [INFERRED] [semantically similar]
  README.md → backend/src/main/resources/application.yml
- `Pokedex Backend Service` --shares_data_with--> `Datasource Configuration`  [INFERRED]
  docker-compose.yml → backend/src/main/resources/application.yml
- `Pokedex Backend Service` --shares_data_with--> `Images Storage Configuration`  [INFERRED]
  docker-compose.yml → backend/src/main/resources/application.yml

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Docker Runtime Services** — docker_compose_pokedex_backend_service, docker_compose_pokedex_frontend_service, docker_compose_libretranslate_service, docker_compose_pokedex_network [EXTRACTED 1.00]
- **Backend Runtime Configuration** — docker_compose_pokedex_backend_service, backend_src_main_resources_application_datasource, backend_src_main_resources_application_images, backend_src_main_resources_application_translation [INFERRED 0.95]

## Communities (76 total, 42 thin omitted)

### Community 0 - "Translation Service"
Cohesion: 0.06
Nodes (32): I18nController, Mono, PostMapping, RequestMapping, RequiredArgsConstructor, RestController, RequiredArgsConstructor, Service (+24 more)

### Community 1 - "Pokemon View Components"
Cohesion: 0.06
Nodes (38): CompareView(), Props, statOrder, FavoritesView(), Props, pokemonApi, PokemonCard(), Props (+30 more)

### Community 2 - "Pokemon API Controller"
Cohesion: 0.07
Nodes (33): GetMapping, Mono, RequestMapping, RequiredArgsConstructor, RestController, PokemonController, Mono, PokemonCatalogRepository (+25 more)

### Community 3 - "Trunfo Game Logic"
Cohesion: 0.08
Nodes (40): trunfoApi, TrunfoApiCard, createTrunfoCardFromApi(), BattlePanel(), Props, GameSetup(), Props, Props (+32 more)

### Community 4 - "Pokemon Catalog Repository"
Cohesion: 0.12
Nodes (19): JdbcClient, PokemonPage, PokemonSummary, Repository, RequiredArgsConstructor, Transactional, JdbcPokemonCatalogCacheRepository, JsonNode (+11 more)

### Community 5 - "Pokemon Image Cache"
Cohesion: 0.10
Nodes (22): GetMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, PokemonImageController, CachePokemonImageUseCase, RequiredArgsConstructor (+14 more)

### Community 6 - "Translation Maintenance"
Cohesion: 0.10
Nodes (22): RequiredArgsConstructor, TranslationMaintenanceController, CleanupResult, JdbcClient, PokemonDetailRepository, RequiredArgsConstructor, Service, Slf4j (+14 more)

### Community 7 - "Admin Security Filter"
Cohesion: 0.10
Nodes (23): AdminApiAccessPolicy, Component, AdminApiTokenFilter, Component, Mono, Override, RequiredArgsConstructor, Component (+15 more)

### Community 8 - "Image Storage Repository"
Cohesion: 0.11
Nodes (15): ImageStorageGateway, PokemonImageRepository, PokemonImage, JdbcClient, Override, Repository, RequiredArgsConstructor, JdbcPokemonImageRepository (+7 more)

### Community 9 - "PokeApi Data Mappers"
Cohesion: 0.13
Nodes (19): CachePokemonImageUseCase, Component, JsonNode, PokemonDetail, PokemonStat, RequiredArgsConstructor, PokeApiPokemonDetailMapper, CachePokemonImageUseCase (+11 more)

### Community 10 - "Translation Job Status"
Cohesion: 0.12
Nodes (16): JdbcClient, RequiredArgsConstructor, Service, TranslationJobStatus, TranslationJobStatusService, Component, JdbcClient, Override (+8 more)

### Community 11 - "Frontend Dependencies"
Cohesion: 0.07
Nodes (26): dependencies, lucide-react, react, react-dom, typescript, vite, @vitejs/plugin-react, devDependencies (+18 more)

### Community 12 - "Pokemon Localization UseCase"
Cohesion: 0.16
Nodes (11): GetLocalizedPokemonDetailUseCase, Mono, RequiredArgsConstructor, Service, Component, TrunfoCard, TrunfoCardFactory, PokemonStat (+3 more)

### Community 13 - "Trunfo Card Controller"
Cohesion: 0.15
Nodes (14): GetMapping, Mono, RequestMapping, RequiredArgsConstructor, RestController, TrunfoController, Service, TrunfoCard (+6 more)

### Community 14 - "Global Error Handling"
Cohesion: 0.29
Nodes (13): ApiError, GlobalExceptionHandler, Mono, ResponseEntity, ServerWebExchange, Slf4j, DataAccessException, DataBufferLimitException (+5 more)

### Community 15 - "TypeScript Configuration"
Cohesion: 0.09
Nodes (21): compilerOptions, allowJs, allowSyntheticDefaultImports, esModuleInterop, forceConsistentCasingInFileNames, isolatedModules, jsx, lib (+13 more)

### Community 16 - "Pokemon Detail Repository"
Cohesion: 0.20
Nodes (8): JdbcClient, PokemonDetail, PokemonSpecies, PokemonStat, Repository, RequiredArgsConstructor, Transactional, JdbcPokemonDetailCacheRepository

### Community 17 - "Species Text Localization"
Cohesion: 0.20
Nodes (9): Component, PtBrTranslationGateway, RequiredArgsConstructor, Slf4j, SpeciesTextLocalizer, BeforeEach, JdbcTemplate, Test (+1 more)

### Community 18 - "External Translation Gateways"
Cohesion: 0.28
Nodes (7): ExternalPtBrTranslationGateway, Builder, Component, Override, RequiredArgsConstructor, Slf4j, LibreTranslateRequest

### Community 19 - "System Infrastructure"
Cohesion: 0.19
Nodes (15): Datasource Configuration, Images Storage Configuration, Translation Configuration, LibreTranslate Service, Pokedex Backend Service, Pokedex Frontend Service, Pokedex Network, Main TSX Script (+7 more)

### Community 20 - "Image Cache Tests"
Cohesion: 0.30
Nodes (8): FakeImageStorageGateway, InMemoryImageRepository, Override, PokemonImage, Test, PokemonImageCacheServiceTest, ImageStorageGateway, PokemonImageRepository

### Community 21 - "PokeApi Web Client"
Cohesion: 0.32
Nodes (8): Builder, Component, JsonNode, Mono, RequiredArgsConstructor, Slf4j, WebClient, PokeApiClient

### Community 22 - "API Integration Tests"
Cohesion: 0.29
Nodes (6): BeforeEach, Test, PokemonControllerTest, SpringBootTest, TestPropertySource, WebTestClient

### Community 23 - "App Layout Routing"
Cohesion: 0.33
Nodes (6): AppChrome(), AppView, navClass(), Props, AppRoutes(), Props

### Community 24 - "WebClient Configuration"
Cohesion: 0.43
Nodes (5): Bean, Builder, Configuration, WebClient, WebClientConfig

### Community 25 - "String Formatting Utils"
Cohesion: 0.48
Nodes (5): formatAbilityName(), formatGenerationName(), formatHabitatName(), formatPokemonName(), formatStatName()

### Community 26 - "Background Task Config"
Cohesion: 0.53
Nodes (4): BackgroundTaskConfig, Bean, Configuration, TaskExecutor

### Community 27 - "CORS Configuration"
Cohesion: 0.53
Nodes (4): CorsConfig, Bean, Configuration, CorsWebFilter

### Community 28 - "Pokemon Type Definitions"
Cohesion: 0.33
Nodes (5): PokemonDetail, PokemonPage, PokemonSpecies, PokemonStat, PokemonSummary

## Knowledge Gaps
- **71 isolated node(s):** `br.com.ruszel:pokedex-backend`, `name`, `private`, `version`, `type` (+66 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **42 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CachePokemonImageUseCase` connect `Pokemon Image Cache` to `Translation Job Status`?**
  _High betweenness centrality (0.171) - this node is a cross-community bridge._
- **Why does `PokemonImageCacheService` connect `Pokemon Image Cache` to `Image Cache Tests`?**
  _High betweenness centrality (0.134) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `TranslationCacheService` (e.g. with `.setUp()` and `.doesNotPersistFallbackSourceAsTranslation()`) actually correct?**
  _`TranslationCacheService` has 5 INFERRED edges - model-reasoned connections that need verification._
- **What connects `br.com.ruszel:pokedex-backend`, `name`, `private` to the rest of the system?**
  _71 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Translation Service` be split into smaller, more focused modules?**
  _Cohesion score 0.061457418788410885 - nodes in this community are weakly interconnected._
- **Should `Pokemon View Components` be split into smaller, more focused modules?**
  _Cohesion score 0.06442307692307692 - nodes in this community are weakly interconnected._
- **Should `Pokemon API Controller` be split into smaller, more focused modules?**
  _Cohesion score 0.06779661016949153 - nodes in this community are weakly interconnected._