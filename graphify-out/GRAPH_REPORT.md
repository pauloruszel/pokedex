# Graph Report - .  (2026-07-12)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 852 nodes · 1500 edges · 85 communities (37 shown, 48 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 92 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `8f4c415f`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Trunfo Game Components
- Internationalization and Translation Services
- Pokemon Detail Data Access
- Pokemon Image Caching
- Translation Cache Maintenance
- Pokemon Catalog Repository
- Admin API Security
- Translation Cache Management
- Frontend API and Context
- Pokemon Image Storage
- Pokemon Catalog Domain
- Pokemon Detail API
- PokeAPI Data Mappers
- Pokemon UI Views
- Frontend Project Dependencies
- Global Error Handling
- Trunfo Game API
- TypeScript Configuration
- Pokemon Detail Cache
- External Translation Gateways
- System Infrastructure Configuration
- Image Cache Testing
- Pokemon Catalog API
- PokeAPI Web Client
- Controller Integration Tests
- App Layout and Routing
- Web Client Configuration
- Frontend Formatting Utilities
- Background Task Configuration
- CORS Security Configuration
- Pokemon TypeScript Models
- Spring Boot Application Entry
- Pokemon Type Themes
- CI/CD Build Workflows
- Maven Wrapper Scripts
- Spring Web Mapping
- Reactive Stream Types
- Request Path Mapping
- Constructor Dependency Injection
- REST Controller Annotation
- HTTP Get Mapping
- HTTP Post Mapping
- Web Request Mapping
- API Controller Definition
- Server Web Exchange
- Web Filter Chain
- Task Execution Service
- Pokemon Detail Model
- Reactive Mono Type
- Required Args Constructor
- JDBC Database Client
- Portuguese Translation Gateway
- Dependency Injection Boilerplate
- Database Client Access
- Constructor Injection Utility
- Spring Component Annotation
- JDBC Client Utility
- Database Query Client
- Pokemon Detail Entity
- Pokemon Pagination Model
- Pokemon Species Model
- Pokemon Stat Model
- Pokemon Summary Model
- Data Repository Annotation
- Lombok Constructor Injection
- Database Transaction Management
- Spring Bean Component
- JSON Node Processing
- Pokemon Detail Schema
- Pokemon Species Schema
- Pokemon Stat Schema
- Pokemon Summary Schema
- Required Constructor Injection
- Reactive Flux Stream
- Reactive Mono Stream
- Java Method Override
- Application Monitoring Config
- Frontend HTML Document
- Project Maven Metadata

## God Nodes (most connected - your core abstractions)
1. `PokeApiPokemonRepository` - 23 edges
2. `TranslationCacheService` - 20 edges
3. `PokedexBootstrapRunner` - 18 edges
4. `JdbcPokemonDetailCacheRepository` - 18 edges
5. `TranslationMaintenanceService` - 17 edges
6. `PokemonImage` - 16 edges
7. `compilerOptions` - 16 edges
8. `PokemonImageCacheService` - 15 edges
9. `ExternalPtBrTranslationGateway` - 14 edges
10. `SpeciesTextLocalizer` - 14 edges

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

## Communities (85 total, 48 thin omitted)

### Community 0 - "Trunfo Game Components"
Cohesion: 0.07
Nodes (37): trunfoApi, TrunfoApiCard, createTrunfoCardFromApi(), BattlePanel(), Props, GameSetup(), Props, Props (+29 more)

### Community 1 - "Internationalization and Translation Services"
Cohesion: 0.08
Nodes (26): I18nController, Mono, PostMapping, RequestMapping, RequiredArgsConstructor, RestController, RequiredArgsConstructor, Service (+18 more)

### Community 2 - "Pokemon Detail Data Access"
Cohesion: 0.08
Nodes (25): Mono, PokemonDetailRepository, GetPokemonDetailUseCase, Mono, RequiredArgsConstructor, Service, JdbcClient, RequiredArgsConstructor (+17 more)

### Community 3 - "Pokemon Image Caching"
Cohesion: 0.10
Nodes (22): GetMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, PokemonImageController, CachePokemonImageUseCase, RequiredArgsConstructor (+14 more)

### Community 4 - "Translation Cache Maintenance"
Cohesion: 0.10
Nodes (23): RequiredArgsConstructor, TranslationMaintenanceController, CleanupResult, JdbcClient, PokemonDetailRepository, RequiredArgsConstructor, Service, Slf4j (+15 more)

### Community 5 - "Pokemon Catalog Repository"
Cohesion: 0.12
Nodes (19): JdbcClient, PokemonPage, PokemonSummary, Repository, RequiredArgsConstructor, Transactional, JdbcPokemonCatalogCacheRepository, JsonNode (+11 more)

### Community 6 - "Admin API Security"
Cohesion: 0.10
Nodes (22): AdminApiAccessPolicy, Component, AdminApiTokenFilter, Component, Mono, Override, RequiredArgsConstructor, Component (+14 more)

### Community 7 - "Translation Cache Management"
Cohesion: 0.11
Nodes (16): Component, TranslationCacheKeyFactory, CachedTranslation, Autowired, Service, TranslationCacheService, Component, PtBrTranslationGateway (+8 more)

### Community 8 - "Frontend API and Context"
Cohesion: 0.10
Nodes (18): pokemonApi, I18nContext, I18nContextValue, I18nProvider(), useAppLanguage(), useI18n(), useI18nFormat(), useMessages() (+10 more)

### Community 9 - "Pokemon Image Storage"
Cohesion: 0.12
Nodes (15): ImageStorageGateway, PokemonImageRepository, PokemonImage, JdbcClient, Override, Repository, RequiredArgsConstructor, JdbcPokemonImageRepository (+7 more)

### Community 10 - "Pokemon Catalog Domain"
Cohesion: 0.11
Nodes (19): Mono, PokemonCatalogRepository, PokemonRepository, Flux, PokemonTypeRepository, Mono, RequiredArgsConstructor, Service (+11 more)

### Community 11 - "Pokemon Detail API"
Cohesion: 0.12
Nodes (17): GetMapping, Mono, RequestMapping, RequiredArgsConstructor, RestController, PokemonDetailController, GetLocalizedPokemonDetailUseCase, Mono (+9 more)

### Community 12 - "PokeAPI Data Mappers"
Cohesion: 0.13
Nodes (19): CachePokemonImageUseCase, Component, JsonNode, PokemonDetail, PokemonStat, RequiredArgsConstructor, PokeApiPokemonDetailMapper, CachePokemonImageUseCase (+11 more)

### Community 13 - "Pokemon UI Views"
Cohesion: 0.09
Nodes (16): Props, statOrder, Props, PokemonCard(), Props, Props, Props, quickTypes (+8 more)

### Community 14 - "Frontend Project Dependencies"
Cohesion: 0.07
Nodes (26): dependencies, lucide-react, react, react-dom, typescript, vite, @vitejs/plugin-react, devDependencies (+18 more)

### Community 15 - "Global Error Handling"
Cohesion: 0.28
Nodes (14): ApiError, GlobalExceptionHandler, Mono, ResponseEntity, ServerWebExchange, Slf4j, DataAccessException, DataBufferLimitException (+6 more)

### Community 16 - "Trunfo Game API"
Cohesion: 0.15
Nodes (14): GetMapping, Mono, RequestMapping, RequiredArgsConstructor, RestController, TrunfoController, Service, TrunfoCard (+6 more)

### Community 17 - "TypeScript Configuration"
Cohesion: 0.09
Nodes (21): compilerOptions, allowJs, allowSyntheticDefaultImports, esModuleInterop, forceConsistentCasingInFileNames, isolatedModules, jsx, lib (+13 more)

### Community 18 - "Pokemon Detail Cache"
Cohesion: 0.20
Nodes (8): JdbcClient, PokemonDetail, PokemonSpecies, PokemonStat, Repository, RequiredArgsConstructor, Transactional, JdbcPokemonDetailCacheRepository

### Community 19 - "External Translation Gateways"
Cohesion: 0.28
Nodes (7): ExternalPtBrTranslationGateway, Builder, Component, Override, RequiredArgsConstructor, Slf4j, LibreTranslateRequest

### Community 20 - "System Infrastructure Configuration"
Cohesion: 0.19
Nodes (15): Datasource Configuration, Images Storage Configuration, Translation Configuration, LibreTranslate Service, Pokedex Backend Service, Pokedex Frontend Service, Pokedex Network, Main TSX Script (+7 more)

### Community 21 - "Image Cache Testing"
Cohesion: 0.30
Nodes (8): FakeImageStorageGateway, InMemoryImageRepository, Override, PokemonImage, Test, PokemonImageCacheServiceTest, ImageStorageGateway, PokemonImageRepository

### Community 22 - "Pokemon Catalog API"
Cohesion: 0.30
Nodes (10): GetMapping, Mono, RequestMapping, RequiredArgsConstructor, RestController, PokemonCatalogController, ListPokemonsByTypeUseCase, ListPokemonsUseCase (+2 more)

### Community 23 - "PokeAPI Web Client"
Cohesion: 0.32
Nodes (8): Builder, Component, JsonNode, Mono, RequiredArgsConstructor, Slf4j, WebClient, PokeApiClient

### Community 24 - "Controller Integration Tests"
Cohesion: 0.29
Nodes (6): BeforeEach, Test, PokemonControllerTest, SpringBootTest, TestPropertySource, WebTestClient

### Community 25 - "App Layout and Routing"
Cohesion: 0.33
Nodes (6): AppChrome(), AppView, navClass(), Props, AppRoutes(), Props

### Community 26 - "Web Client Configuration"
Cohesion: 0.43
Nodes (5): Bean, Builder, Configuration, WebClient, WebClientConfig

### Community 27 - "Frontend Formatting Utilities"
Cohesion: 0.48
Nodes (5): formatAbilityName(), formatGenerationName(), formatHabitatName(), formatPokemonName(), formatStatName()

### Community 28 - "Background Task Configuration"
Cohesion: 0.53
Nodes (4): BackgroundTaskConfig, Bean, Configuration, TaskExecutor

### Community 29 - "CORS Security Configuration"
Cohesion: 0.53
Nodes (4): CorsConfig, Bean, Configuration, CorsWebFilter

### Community 30 - "Pokemon TypeScript Models"
Cohesion: 0.33
Nodes (5): PokemonDetail, PokemonPage, PokemonSpecies, PokemonStat, PokemonSummary

## Knowledge Gaps
- **75 isolated node(s):** `br.com.ruszel:pokedex-backend`, `name`, `private`, `version`, `type` (+70 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **48 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CachePokemonImageUseCase` connect `Pokemon Image Caching` to `Pokemon Detail Data Access`?**
  _High betweenness centrality (0.165) - this node is a cross-community bridge._
- **Why does `PokemonImageCacheService` connect `Pokemon Image Caching` to `Image Cache Testing`?**
  _High betweenness centrality (0.129) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `TranslationCacheService` (e.g. with `.setUp()` and `.doesNotPersistFallbackSourceAsTranslation()`) actually correct?**
  _`TranslationCacheService` has 5 INFERRED edges - model-reasoned connections that need verification._
- **What connects `br.com.ruszel:pokedex-backend`, `name`, `private` to the rest of the system?**
  _75 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Trunfo Game Components` be split into smaller, more focused modules?**
  _Cohesion score 0.07474600870827286 - nodes in this community are weakly interconnected._
- **Should `Internationalization and Translation Services` be split into smaller, more focused modules?**
  _Cohesion score 0.07616892911010557 - nodes in this community are weakly interconnected._
- **Should `Pokemon Detail Data Access` be split into smaller, more focused modules?**
  _Cohesion score 0.07632850241545894 - nodes in this community are weakly interconnected._