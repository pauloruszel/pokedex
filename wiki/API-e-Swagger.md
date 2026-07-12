# API e Swagger

Com o backend rodando:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Pokémon

```http
GET /api/pokemon?limit=24&offset=0
GET /api/pokemon/search?q=pikachu
GET /api/pokemon/types
GET /api/pokemon/type/{typeName}?limit=24&offset=0
GET /api/pokemon/{nameOrId}
GET /api/pokemon/{pokemonId}/images/{imageType}
```

## Trunfo

```http
GET /api/trunfo/cards?limit=40&offset=0&mode=balanced
GET /api/trunfo/cards?limit=40&offset=0&mode=balanced&type=electric
```

Parâmetros principais:

| Parâmetro | Uso |
| --- | --- |
| `limit` | Quantidade máxima de cartas |
| `offset` | Posição inicial para paginação |
| `mode` | Modo de seleção das cartas |
| `type` | Filtro opcional por tipo |

## i18n

```http
POST /api/i18n/translate
```

## Administração de traduções

```http
GET  /api/admin/translations/missing?limit=2000
POST /api/admin/translations/refresh?limit=2000
GET  /api/admin/translations/status
POST /api/admin/translations/cleanup-invalid-cache
```

Endpoints administrativos exigem:

```http
X-Admin-Token: local-dev-token
```

## Actuator

```http
GET /actuator/health
GET /actuator/metrics
```

