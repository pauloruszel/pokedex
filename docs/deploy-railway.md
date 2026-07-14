# Deploy no Railway

Use dois services no mesmo projeto Railway: um para `backend` e outro para `frontend`.

## Backend

Crie um service a partir do repositório com:

- Root Directory: `/backend`
- Config file path: `/backend/railway.toml`
- Public Networking: gerar domínio público
- Target port: `8080`

Variáveis recomendadas:

```text
SERVER_PORT=8080
POKEAPI_BASE_URL=https://pokeapi.co/api/v2
SPRING_DATASOURCE_URL=jdbc:h2:file:/data/h2/pokedex-db;AUTO_SERVER=FALSE;DB_CLOSE_DELAY=-1
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=
POKEDEX_BOOTSTRAP_ENABLED=true
POKEDEX_BOOTSTRAP_LIMIT=1025
POKEDEX_BOOTSTRAP_DETAILS_ENABLED=true
POKEDEX_IMAGES_STORAGE_PATH=/data/pokedex-images
POKEDEX_TRANSLATION_ENABLED=true
POKEDEX_TRANSLATION_LIBRETRANSLATE_URL=
POKEDEX_TRANSLATION_URL=https://api.mymemory.translated.net/get
POKEDEX_TRANSLATION_FALLBACK_URL=https://translate.googleapis.com
POKEDEX_TRANSLATION_TIMEOUT_SECONDS=8
POKEDEX_ADMIN_ENABLED=true
POKEDEX_ADMIN_TOKEN=<gere-um-token-forte>
POKEDEX_CORS_ALLOWED_ORIGINS=https://<dominio-do-frontend>
```

Anexe um volume ao backend com mount path `/data`. O H2 e as imagens usam esse diretório.

## Frontend

Crie outro service a partir do mesmo repositório com:

- Root Directory: `/frontend`
- Config file path: `/frontend/railway.toml`
- Public Networking: gerar domínio público
- Target port: `80`

Variável obrigatória no build do frontend:

```text
VITE_API_BASE_URL=https://<dominio-do-backend>
```

Depois que o Railway gerar o domínio do frontend, volte no backend e ajuste:

```text
POKEDEX_CORS_ALLOWED_ORIGINS=https://<dominio-do-frontend>
```

Para liberar mais de uma origem, separe por vírgula:

```text
POKEDEX_CORS_ALLOWED_ORIGINS=https://app.exemplo.com,https://pokedex.up.railway.app
```

## Validação

Após o deploy:

```text
https://<dominio-do-backend>/actuator/health
https://<dominio-do-backend>/swagger-ui.html
https://<dominio-do-frontend>
```

O frontend chama o backend pela URL configurada em `VITE_API_BASE_URL`; essa variável é embutida durante o build Vite.
