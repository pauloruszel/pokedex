# Contrato de validação

Use esta spec para definir quando uma tarefa está pronta.

## Frontend

```powershell
cd frontend
npm run build
```

Use para:

- alterações em React;
- i18n;
- CSS;
- Trunfo;
- Pokédex, favoritos ou comparação.

## Backend

```powershell
cd backend
.\mvnw.cmd test
```

Use para:

- controllers;
- use cases;
- cache;
- tradução;
- imagem;
- Swagger;
- persistência.

## Docker

```powershell
docker compose config
docker compose up --build
```

Use `up --build` quando a alteração afetar Dockerfile, Compose, Nginx, portas ou variáveis de runtime.

## Graphify

```powershell
& "$(Get-Content graphify-out/.graphify_python)" -m graphify . --update
& "$(Get-Content graphify-out/.graphify_python)" -m graphify cluster-only C:\workspace\projetos_novos\pokedex-clean
& "$(Get-Content graphify-out/.graphify_python)" -m graphify label --backend gemini
```

Use quando a alteração tiver objetivo arquitetural.

## Regra

Se um comando não puder ser executado, registrar o motivo no fechamento da tarefa.

