# Contrato de validação

Use esta spec para definir quando uma tarefa está pronta.

## Regra base

Rode o menor comando confiável para o escopo alterado. Não execute validações caras sem relação com a mudança; registre o motivo no fechamento.

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
```

Use `docker compose config` para validar Compose. Use `docker compose up --build` só quando a alteração afetar Dockerfile, Nginx, portas, variáveis de runtime ou build da imagem.

## Graphify

```powershell
& "$(Get-Content graphify-out/.graphify_python)" -m graphify . --update
& "$(Get-Content graphify-out/.graphify_python)" -m graphify cluster-only C:\workspace\projetos_novos\pokedex-clean
& "$(Get-Content graphify-out/.graphify_python)" -m graphify label --backend gemini
```

Use quando a alteração tiver objetivo arquitetural.

## Regra

Se um comando não puder ser executado, registrar o motivo no fechamento da tarefa.
