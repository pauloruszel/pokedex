package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.domain.model.PokemonDetail;
import br.com.ruszel.pokedex.domain.model.PokemonStat;
import br.com.ruszel.pokedex.domain.model.TrunfoAttributes;
import br.com.ruszel.pokedex.domain.model.TrunfoCard;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class TrunfoCardFactory {
    public TrunfoCard toCard(PokemonDetail detail) {
        TrunfoAttributes attributes = toAttributes(detail);
        String rarity = rarity(attributes.total());

        return new TrunfoCard(
                detail.id(),
                detail.name(),
                detail.imageUrl(),
                detail.types(),
                rarity,
                "lendaria".equals(rarity),
                attributes
        );
    }

    public List<TrunfoCard> selectCards(List<TrunfoCard> cards, int limit, String mode) {
        if (!"balanced".equalsIgnoreCase(mode)) {
            return cards.stream().limit(limit).toList();
        }

        List<TrunfoCard> legendary = cardsByRarity(cards, "lendaria");
        List<TrunfoCard> epic = cardsByRarity(cards, "epica");
        List<TrunfoCard> rare = cardsByRarity(cards, "rara");
        List<TrunfoCard> common = cardsByRarity(cards, "comum");
        List<TrunfoCard> selected = new ArrayList<>();

        while (selected.size() < limit && selected.size() < cards.size()) {
            pickNext(selected, rare, limit);
            pickNext(selected, common, limit);
            pickNext(selected, epic, limit);
            pickNext(selected, legendary, limit);
        }

        if (selected.size() < limit) {
            cards.stream()
                    .filter(card -> !selected.contains(card))
                    .limit(limit - selected.size())
                    .forEach(selected::add);
        }

        return selected;
    }

    private List<TrunfoCard> cardsByRarity(List<TrunfoCard> cards, String rarity) {
        return cards.stream()
                .filter(card -> rarity.equals(card.rarity()))
                .sorted(Comparator.comparing(card -> card.attributes().total(), Comparator.reverseOrder()))
                .toList();
    }

    private void pickNext(List<TrunfoCard> selected, List<TrunfoCard> source, int limit) {
        if (selected.size() >= limit) {
            return;
        }

        source.stream()
                .filter(card -> !selected.contains(card))
                .findFirst()
                .ifPresent(selected::add);
    }

    private TrunfoAttributes toAttributes(PokemonDetail detail) {
        int hp = stat(detail, "hp");
        int attack = stat(detail, "attack");
        int defense = stat(detail, "defense");
        int specialAttack = stat(detail, "special-attack");
        int specialDefense = stat(detail, "special-defense");
        int speed = stat(detail, "speed");
        int total = hp + attack + defense + specialAttack + specialDefense + speed;

        return new TrunfoAttributes(
                hp,
                attack,
                defense,
                specialAttack,
                specialDefense,
                speed,
                detail.weight() / 10.0,
                detail.height() / 10.0,
                total
        );
    }

    private int stat(PokemonDetail detail, String name) {
        return detail.stats().stream()
                .filter(stat -> name.equals(stat.name()))
                .map(PokemonStat::value)
                .findFirst()
                .orElse(0);
    }

    private String rarity(int total) {
        if (total >= 581) {
            return "lendaria";
        }
        if (total >= 481) {
            return "epica";
        }
        if (total >= 351) {
            return "rara";
        }
        return "comum";
    }
}
