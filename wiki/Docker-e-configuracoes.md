# Docker e configurações

## Serviços do Compose

O `docker-compose.yml` sobe:

- backend Spring Boot;
- frontend servido por Nginx;
- volumes para H2 e imagens;
- LibreTranslate opcional.

## Build

O Compose usa `build:` para backend e frontend. Por isso:

```bash
docker compose up --build
```

gera as imagens locais antes de subir os containers.

## Volumes

```yaml
volumes:
  pokedex-h2-data:
  pokedex-images:
  libretranslate-data:
```

## Variáveis principais

| Variável | Padrão | Uso |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | Porta do backend |
| `POKEAPI_BASE_URL` | `https://pokeapi.co/api/v2` | URL da PokeAPI |
| `POKEDEX_BOOTSTRAP_ENABLED` | `true` | Ativa carga inicial |
| `POKEDEX_BOOTSTRAP_LIMIT` | `1025` | Limite do bootstrap |
| `POKEDEX_BOOTSTRAP_DETAILS_ENABLED` | `true` | Carrega detalhes no bootstrap |
| `POKEDEX_IMAGES_STORAGE_PATH` | `./data/pokedex-images` | Diretório de imagens |
| `POKEDEX_ADMIN_ENABLED` | `true` | Ativa endpoints admin |
| `POKEDEX_ADMIN_TOKEN` | `local-dev-token` | Token admin |
| `POKEDEX_CORS_ALLOWED_ORIGINS` | origens locais | Origens HTTP liberadas pelo backend |
| `VITE_API_BASE_URL` | `http://localhost:8080` | URL do backend no frontend |

## LibreTranslate

O LibreTranslate é opcional:

```bash
docker compose --profile optional-translation up --build
```

Se ele não estiver ativo, o backend usa os provedores configurados como fallback.
