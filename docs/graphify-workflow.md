# Workflow Graphify

Use este workflow para análise arquitetural, coesão, comunidades e refatorações guiadas por grafo.

## Atualizar grafo

```powershell
& "$(Get-Content graphify-out/.graphify_python)" -m graphify . --update
```

## Recalcular comunidades

```powershell
& "$(Get-Content graphify-out/.graphify_python)" -m graphify cluster-only C:\workspace\projetos_novos\pokedex-clean
```

## Atualizar labels

```powershell
& "$(Get-Content graphify-out/.graphify_python)" -m graphify label --backend gemini
```

## Como interpretar

- Coesão baixa sugere investigação, não refatoração automática.
- God node precisa ser lido no código antes de qualquer corte.
- Arestas `INFERRED` precisam de confirmação manual.
- Comunidades com annotations Spring podem parecer menos coesas sem indicar problema real.

## Prompt modelo

```text
Objetivo:
Analisar o módulo [nome] no Graphify e propor o menor plano de refatoração.

Contexto:
- Coesão: [valor]
- Comunidade: [nome]
- God nodes: [lista]
- Arestas suspeitas: [lista]

Restrições:
- Confirmar no código antes de alterar.
- Não mexer em módulos fora do escopo.
- Priorizar deleção, extração simples ou mover função existente.

Entrega:
- Diagnóstico.
- Plano mínimo.
- Implementação se o plano for direto.
- Validação.
```

