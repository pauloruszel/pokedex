# Playbook de prompts do projeto

Este guia transforma os aprendizados de engenharia com IA em prompts melhores para este projeto.

## Regra base

Todo prompt bom deve dizer:

- **Objetivo:** o que precisa mudar.
- **Escopo:** onde mexer e onde não mexer.
- **Contexto:** arquivos, relatório do Graphify, erro, decisão anterior ou comportamento atual.
- **Restrições:** simplicidade, sem dependências novas, manter API, manter Docker, etc.
- **Validação:** comandos que devem passar.
- **Entrega:** formato esperado da resposta.
- **Parada:** quando considerar pronto.

Modelo:

```text
Objetivo:
[o resultado esperado]

Contexto:
[arquivos, relatório, bug, decisão, fluxo atual]

Escopo:
- Pode mexer em: [...]
- Não mexer em: [...]

Restrições:
- Use a menor mudança segura.
- Reuse padrões existentes.
- Não adicione dependências sem justificar.

Validação:
- [comando 1]
- [comando 2]

Entrega:
- Aplique as mudanças.
- Liste arquivos alterados.
- Informe validações executadas.
```

## Refatoração com Graphify

Use quando o Graphify apontar baixa coesão, god nodes ou acoplamento suspeito.

```text
Objetivo:
Refatorar o módulo [nome do módulo] para reduzir acoplamento e melhorar coesão.

Contexto:
O Graphify apontou:
- Comunidade: [nome]
- Coesão: [valor]
- God nodes: [nós]
- Conexões suspeitas: [arestas ou arquivos]

Escopo:
- Pode mexer nos arquivos: [lista]
- Não mexer em comportamento público, endpoints ou contratos do frontend.

Restrições:
- Use Ponytail: menor corte que melhora a estrutura.
- Confirme no código se as relações inferidas pelo Graphify são reais.
- Não crie abstração com uma única implementação sem necessidade.
- Use baseline curta; não carregue o GRAPH_REPORT.md inteiro.

Validação:
- Rodar só os comandos relevantes ao escopo alterado.
- Se alterar código por causa do Graphify, rodar update, cluster e label.

Entrega:
- Aplique a refatoração.
- Explique qual acoplamento foi removido.
- Informe a decisão final e, se rodar Graphify, compare antes/depois.
```

## Bug ou comportamento quebrado

```text
Objetivo:
Corrigir [bug] em [fluxo/tela/endpoint].

Contexto:
Comportamento atual:
[descrever]

Comportamento esperado:
[descrever]

Arquivos prováveis:
[lista curta]

Restrições:
- Corrija na causa raiz.
- Antes de editar, procure todos os callers do método/componente afetado.
- Não espalhe guards em vários lugares se houver um ponto comum.

Validação:
- [teste ou build mínimo]

Entrega:
- Aplique a correção.
- Mostre a causa raiz.
- Informe a validação executada.
```

## Frontend

Use para mudanças em React, Trunfo, Pokédex, favoritos, comparação ou i18n.

```text
Objetivo:
Alterar [tela/fluxo] para [resultado].

Contexto:
Fluxo atual:
[descrever]

Escopo:
- Pode mexer em frontend/src/features/[feature]
- Pode mexer em frontend/src/shared/i18n/messages.ts se houver texto novo.
- Não mexer no backend salvo se a API atual não suportar o fluxo.

Restrições:
- Manter componentes pequenos.
- Evitar estado global novo.
- Reusar hooks e componentes existentes.
- Textos em pt-BR, es e en quando estiverem em messages.ts.

Validação:
cd frontend
npm run build

Entrega:
- Implementar.
- Informar arquivos alterados e build.
```

## Backend

Use para endpoints, cache, tradução, imagem, Swagger ou regras de aplicação.

```text
Objetivo:
Alterar [endpoint/use case/serviço] para [resultado].

Contexto:
[erro, endpoint, classe, relatório Graphify ou decisão]

Escopo:
- Pode mexer em backend/src/main/java/[pacote]
- Pode mexer em testes relacionados.
- Não alterar contrato público sem atualizar Swagger e frontend.

Restrições:
- Manter Clean Architecture: controller -> use case -> porta -> adapter.
- Não colocar regra de negócio em controller.
- Reusar ports/use cases existentes.

Validação:
cd backend
.\mvnw.cmd test

Entrega:
- Implementar.
- Atualizar Swagger se endpoint mudar.
- Informar testes executados.
```

## Docker e execução local

```text
Objetivo:
Melhorar Docker/Compose para [resultado].

Contexto:
Comando usado:
docker compose up --build

Problema:
[descrever]

Restrições:
- Não quebrar execução local sem Docker.
- Não adicionar serviço novo sem necessidade.
- Manter variáveis documentadas no README/Wiki.

Validação:
docker compose config
docker compose up --build

Entrega:
- Aplicar ajustes.
- Explicar impacto no build do frontend/backend.
```

## Swagger e documentação

```text
Objetivo:
Melhorar documentação Swagger/Wiki/README para [tema].

Contexto:
[endpoints, mudança feita, lacuna atual]

Escopo:
- Swagger: backend/src/main/java/... e OpenApiConfig.
- Wiki: wiki/*.md.
- README: somente se a instrução for essencial para rodar o projeto.

Restrições:
- Escrever em pt-BR.
- Não usar tom de produto/marketing.
- Preferir instruções executáveis e exemplos reais.

Validação:
- Backend sobe e /swagger-ui.html abre.
- Links locais documentados estão corretos.

Entrega:
- Atualizar docs.
- Informar páginas/arquivos alterados.
```

## Prompt bom para este projeto

Exemplo:

```text
Objetivo:
Melhorar o modo manual do Trunfo para o jogador escolher cartas em mais de uma página.

Contexto:
Hoje o draft manual chama trunfoApi.cards(40, ...) e limita a escolha a uma amostra.
O backend já aceita limit e offset em /api/trunfo/cards.

Escopo:
- Pode mexer em features/trunfo.
- Pode mexer em messages.ts e global.css.
- Não mexer no backend.

Restrições:
- Menor impacto possível.
- Sem paginação numerada; usar busca + carregar mais.
- Manter modo automático igual.

Validação:
cd frontend
npm run build

Entrega:
- Implementar.
- Informar validação.
```

## Prompt ruim e versão melhor

Ruim:

```text
Melhore o Trunfo.
```

Melhor:

```text
Quero melhorar o modo manual do Trunfo.
O problema é que aparecem poucas cartas para escolher.
Use o endpoint existente com offset, adicione busca por nome/número e botão carregar mais.
Não altere o modo automático.
Valide com npm run build no frontend.
```
