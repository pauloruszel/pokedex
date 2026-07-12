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
- Package metadata, configs e documentação podem criar comunidades ruidosas; não usar isso sozinho como gatilho de refatoração.

## Para refatoração de código

- Registrar baseline antes de editar: commit, comunidade, coesão e god nodes.
- Ler só as seções necessárias do relatório: Summary, God Nodes, Communities e Suggested Questions.
- Usar comandos de leitura seletiva; não despejar o `GRAPH_REPORT.md` inteiro no contexto.
- Tratar docs e wiki como ruído quando o objetivo for coesão de código.
- Não perseguir coesão baixa causada principalmente por annotations, configs ou package metadata.
- Parar quando o menor corte seguro for validado, mesmo que a coesão continue baixa por ruído de framework.
- Se a tarefa nasceu do Graphify ou tem objetivo arquitetural, rodar update, cluster e label depois da mudança, salvo pedido explícito para não rodar.
- No fechamento, mostrar comparação antes/depois ou explicar por que Graphify não foi executado.

## Baseline curta

Use `rg` para localizar metadados gerais:

```powershell
rg "Built from commit|## Summary|## God Nodes|Suggested Questions" graphify-out\GRAPH_REPORT.md -n -C 2
```

Para um módulo específico, prefira imprimir apenas o bloco da comunidade:

```powershell
$lines = Get-Content -Encoding UTF8 graphify-out\GRAPH_REPORT.md
$start = ($lines | Select-String '### Community .*NOME_DO_MODULO').LineNumber
if ($start) { $lines[($start-1)..([Math]::Min($start+2, $lines.Length-1))] }
```

Para god nodes, limite a seção:

```powershell
$lines = Get-Content -Encoding UTF8 graphify-out\GRAPH_REPORT.md
$start = ($lines | Select-String '## God Nodes').LineNumber
if ($start) { $lines[($start-1)..([Math]::Min($start+10, $lines.Length-1))] }
```

Evite padrões genéricos como `Cohesion` sem restringir a comunidade: eles retornam quase todas as comunidades do relatório.
Evite `Select-String -InputObject $lines -Context ...` com arrays grandes: no PowerShell isso pode retornar blocos enormes e gastar contexto sem necessidade.
Prefira comandos PowerShell simples quando houver aspas ou regex; se um comando de inspeção falhar por escaping, repita com uma forma menor e registre a falha no fechamento.

## Decisão final

Ao fechar uma análise Graphify, declare uma destas opções:

- Refatorar agora: há acoplamento real confirmado no código.
- Não refatorar: baixa coesão é ruído de framework, config, metadata ou docs.
- Adiar: há sinal real, mas sem dor concreta ou sem menor corte seguro.

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
- Registrar baseline antes de editar.
- Usar baseline curta do Graphify.

Entrega:
- Diagnóstico.
- Decisão final: refatorar agora, não refatorar ou adiar.
- Plano mínimo.
- Implementação se o plano for direto.
- Validação.
- Comparação antes/depois quando Graphify for atualizado.
- Comandos não executados e motivo.
```
