# Agent Contract

Use este contrato para qualquer sessão de agente neste repositório.

## Trabalho padrão

- Entender o fluxo real nos arquivos afetados antes de editar.
- Preferir a menor mudança segura.
- Reusar padrões existentes do projeto.
- Não adicionar dependência sem justificativa objetiva.
- Corrigir causa raiz, não só o sintoma.
- Não alterar contrato público sem atualizar frontend, backend, Swagger e docs afetadas.
- Preservar mudanças não relacionadas feitas pelo usuário.

## Validação

- Frontend: `cd frontend && npm run build`.
- Backend: `cd backend && .\mvnw.cmd test`.
- Docker: `docker compose config` antes de alterações maiores no Compose.
- Graphify: usar quando a tarefa envolver arquitetura, acoplamento, coesão ou módulos.

## Graphify

Quando houver `graphify-out/`, tratar o grafo como fonte auxiliar, não como verdade absoluta.

- Ler `graphify-out/GRAPH_REPORT.md` para perguntas arquiteturais.
- Confirmar relações inferidas no código antes de refatorar.
- Depois de refatorações relevantes, rodar:

```powershell
& "$(Get-Content graphify-out/.graphify_python)" -m graphify . --update
& "$(Get-Content graphify-out/.graphify_python)" -m graphify cluster-only C:\workspace\projetos_novos\pokedex-clean
& "$(Get-Content graphify-out/.graphify_python)" -m graphify label --backend gemini
```

## Prompts e specs

Usar `docs/prompt-playbook.md` como referência para formatar pedidos com objetivo, contexto, escopo, restrições, validação e entrega.

