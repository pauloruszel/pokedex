# Agent Contract

Use este contrato para qualquer sessão de agente neste repositório.

## Trabalho padrão

- Entender o fluxo real nos arquivos afetados antes de editar.
- Preferir a menor mudança segura.
- Reusar padrões existentes do projeto.
- Não adicionar dependência sem justificativa objetiva.
- Corrigir causa raiz, não só o sintoma.
- Não alterar contrato público sem atualizar frontend, backend, Swagger e docs afetadas.
- Antes de editar, verificar o working tree e não misturar mudanças não relacionadas.
- Preservar mudanças não relacionadas feitas pelo usuário.

## Validação

- Rodar o menor comando confiável para o escopo alterado.
- Frontend: `cd frontend && npm run build` quando tocar código frontend.
- Backend: `cd backend && .\mvnw.cmd test` quando tocar código backend.
- Docker: `docker compose config` antes de alterações no Compose.
- Graphify: usar quando a tarefa envolver arquitetura, acoplamento, coesão ou módulos.

## Graphify

Quando houver `graphify-out/`, tratar o grafo como fonte auxiliar, não como verdade absoluta.

- Para perguntas arquiteturais, usar `docs/graphify-workflow.md`.
- Ler só as seções necessárias do `GRAPH_REPORT.md`; preferir a baseline curta.
- Confirmar relações inferidas no código antes de refatorar.
- Para refatoração de código, registrar baseline antes de editar: commit, comunidade, coesão e god nodes.
- Se a tarefa nasceu de Graphify ou tem objetivo arquitetural, rodar Graphify depois da mudança, salvo pedido explícito para não rodar:

```powershell
& "$(Get-Content graphify-out/.graphify_python)" -m graphify . --update
& "$(Get-Content graphify-out/.graphify_python)" -m graphify cluster-only C:\workspace\projetos_novos\pokedex-clean
& "$(Get-Content graphify-out/.graphify_python)" -m graphify label --backend gemini
```

## Prompts e specs

Usar `docs/prompt-playbook.md` como referência para formatar pedidos com objetivo, contexto, escopo, restrições, validação e entrega.

## Fechamento obrigatório

Ao finalizar tarefa, informar:

- working tree inicial relevante;
- arquivos alterados;
- validação executada;
- comandos que não foram executados e motivo;
- decisão final quando for análise Graphify: refatorar agora, não refatorar ou adiar;
- se era refatoração arquitetural, comparação Graphify antes/depois ou motivo para não rodar.
