# Arquitetura

O projeto segue uma separação próxima de Clean Architecture.

## Backend

Fluxo principal:

```text
Controllers
  -> Use cases
  -> Portas de aplicação
  -> Adapters de infraestrutura
  -> H2, PokeAPI, filesystem e serviços externos
```

Pacotes principais:

| Pacote | Responsabilidade |
| --- | --- |
| `api/controller` | Endpoints REST |
| `api/filter` | Filtros HTTP, autenticação admin e logging |
| `api/error` | Tratamento padronizado de erros |
| `application/usecase` | Casos de uso e orquestração |
| `application/port` | Contratos da aplicação |
| `domain/model` | Modelos de domínio |
| `infrastructure/pokeapi` | Cliente e mappers da PokeAPI |
| `infrastructure/persistence` | Cache JDBC em H2 |
| `infrastructure/storage` | Cache físico de imagens |
| `infrastructure/localization` | Tradução e localização de textos |

## Frontend

| Diretório | Responsabilidade |
| --- | --- |
| `app` | Shell, navegação e roteamento |
| `features/pokemon` | Consulta, listagem e detalhe de Pokémon |
| `features/favorites` | Favoritos locais |
| `features/compare` | Comparação de Pokémon |
| `features/trunfo` | Cartas, setup, draft e lógica do Trunfo |
| `shared/api` | Configuração da API |
| `shared/i18n` | Idioma e mensagens |
| `shared/components` | Componentes reutilizáveis |
| `shared/utils` | Helpers de formatação e assets |

## Pontos de atenção

O Graphify ainda aponta alguns módulos com baixa coesão, principalmente em áreas com muitas anotações Spring ou componentes que fazem integração entre várias partes do sistema.

Os principais pontos monitorados são:

- manutenção de traduções;
- cache de tradução;
- cache de imagens;
- controllers com muitas dependências;
- lógica do Trunfo.

