# Contrato de refatoração

Use esta spec quando a tarefa pedir refatoração.

## Entrada mínima

- Módulo, arquivo ou fluxo alvo.
- Sintoma: coesão baixa, god node, teste difícil, bug, duplicação ou acoplamento.
- Restrições: o que não pode mudar.

## Processo

1. Ler os arquivos diretamente afetados.
2. Buscar callers e imports do ponto alterado.
3. Se a motivação veio do Graphify, confirmar no código se o acoplamento é real.
4. Fazer o menor corte que melhora o problema.
5. Evitar novas abstrações sem ganho claro.
6. Parar quando o menor corte seguro estiver validado.
7. Validar com o menor comando confiável.
8. Fechar a tarefa com arquivos alterados, validação e pendências explícitas.

## Se usar Graphify

- Registrar baseline antes de editar: commit, comunidade, coesão e god nodes.
- Ler só as seções necessárias do relatório para reduzir custo: Summary, God Nodes, Communities e Suggested Questions.
- Usar baseline curta do `docs/graphify-workflow.md`; não carregar o relatório inteiro.
- Ignorar baixa coesão causada principalmente por annotations, configs, package metadata ou documentação.
- Confirmar no código qualquer acoplamento sugerido pelo grafo.
- Se a refatoração nasceu do Graphify, rodar update, cluster e label depois da mudança, salvo pedido explícito para não rodar.
- Comparar antes/depois apenas depois de rodar update, cluster e label.

## Saída esperada

- Código alterado.
- Arquivos tocados.
- Validação executada.
- Comandos não executados e motivo.
- Comparação Graphify antes/depois, se aplicável.
- O que foi deliberadamente deixado de fora.

## Prompt modelo

```text
Objetivo:
Refatorar [módulo/fluxo] para [resultado].

Contexto:
[Graphify, bug, dor de teste ou acoplamento observado]

Escopo:
- Pode mexer em: [...]
- Não mexer em: [...]

Restrições:
- Menor mudança segura.
- Sem dependência nova.
- Sem alterar contrato público.
- Parar quando o menor corte seguro estiver validado.

Validação:
[comandos]
```
