# Graphify e manutenção

O projeto usa Graphify para acompanhar dependências, comunidades e coesão dos módulos.

Arquivos gerados:

```text
graphify-out/GRAPH_REPORT.md
graphify-out/graph.html
graphify-out/graph.json
```

Esses arquivos são locais e não precisam ser versionados.

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

## Leitura atual do grafo

Última análise local:

- 906 nós;
- 1549 arestas;
- 102 comunidades;
- sem ciclos de importação detectados.

God nodes atuais:

- `PokeApiPokemonRepository`;
- `TranslationCacheService`;
- `PokedexBootstrapRunner`;
- `JdbcPokemonDetailCacheRepository`;
- `TranslationMaintenanceService`.

Esses nós merecem atenção em refatorações futuras porque concentram muitas conexões no grafo.

