# Funcionalidades

## Pokédex

- Lista Pokémon com paginação.
- Busca por nome ou número.
- Filtro por tipo.
- Detalhes com status, habilidades, habitat, geração, galeria e linha evolutiva.
- Textos localizados quando disponíveis.

## Favoritos

- Permite marcar Pokémon como favoritos.
- Os favoritos ficam salvos no navegador.
- A lista de favoritos pode ser usada no modo Trunfo.

## Comparação

- Permite selecionar dois Pokémon.
- Compara status base, tipo, altura e peso.
- Destaca o vencedor de cada atributo.

## Super Trunfo

O modo Trunfo usa dados da Pokédex para montar cartas com atributos calculados.

Modos de deck:

- **Automático:** a API monta e embaralha os decks.
- **Personalizado:** o jogador escolhe suas cartas e a CPU recebe um deck equivalente.

No modo personalizado:

- o jogador pode buscar cartas por nome ou número;
- pode carregar mais cartas usando paginação;
- a CPU monta um deck com cartas próximas em raridade e força;
- a partida continua usando a mesma máquina de estado do jogo.

## Tradução

- O backend possui fluxo de tradução pt-BR para textos de espécie.
- Traduções são cacheadas para evitar chamadas repetidas.
- Endpoints administrativos permitem consultar pendências e atualizar traduções.

