package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.domain.model.TrunfoCard;
import br.com.ruszel.pokedex.domain.model.TrunfoRoundView;
import br.com.ruszel.pokedex.domain.model.TrunfoRoomView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrunfoRoomService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final TypeReference<List<TrunfoCard>> CARD_LIST = new TypeReference<>() {};
    private static final TypeReference<List<TrunfoRoundView>> ROUND_LIST = new TypeReference<>() {};
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int FAST_DECK_SIZE = 8;
    private static final int NORMAL_DECK_SIZE = 20;

    private final JdbcClient jdbcClient;
    private final ListTrunfoCardsUseCase listTrunfoCardsUseCase;

    @Transactional
    public TrunfoRoomView create(String nickname, String mode, String difficulty, String type, Integer deckSize) {
        String code;
        do {
            code = randomCode();
        } while (exists(code));

        int sanitizedDeckSize = sanitizeDeckSize(deckSize);
        String token = UUID.randomUUID().toString();
        jdbcClient.sql("""
                        INSERT INTO trunfo_room
                            (code, state, mode, difficulty, deck_size, type_name, player_one_name, player_one_token, dispute_pile, history, current_turn, expires_at)
                        VALUES
                            (:code, 'WAITING_FOR_PLAYER', :mode, :difficulty, :deckSize, :type, :name, :token, '[]', '[]', 'player-one', :expiresAt)
                        """)
                .param("code", code)
                .param("mode", blankToDefault(mode, "all"))
                .param("difficulty", blankToDefault(difficulty, "balanced"))
                .param("deckSize", sanitizedDeckSize)
                .param("type", hasText(type) ? type : null)
                .param("name", blankToDefault(nickname, "Jogador 1"))
                .param("token", token)
                .param("expiresAt", Instant.now().plus(30, ChronoUnit.MINUTES))
                .update();
        return view(read(code), "player-one", token, false);
    }

    @Transactional
    public Mono<TrunfoRoomView> join(String code, String nickname) {
        Room room = read(code);
        if (room.playerTwoToken() != null) {
            throw new IllegalStateException("Sala cheia.");
        }
        String token = UUID.randomUUID().toString();
        int deckSize = sanitizeDeckSize(room.deckSize());
        return listTrunfoCardsUseCase.execute(deckSize * 2, room.difficulty(), room.typeName(), 0)
                .map(cards -> {
                    if (cards.size() < deckSize * 2) {
                        throw new IllegalStateException("Cartas insuficientes para iniciar a partida.");
                    }
                    List<TrunfoCard> shuffled = new ArrayList<>(cards);
                    Collections.shuffle(shuffled, RANDOM);
                    jdbcClient.sql("""
                                    UPDATE trunfo_room
                                       SET state = 'IN_PROGRESS',
                                           player_two_name = :name,
                                           player_two_token = :token,
                                           player_one_deck = :p1,
                                           player_two_deck = :p2,
                                           updated_at = CURRENT_TIMESTAMP
                                     WHERE code = :code
                                    """)
                            .param("name", blankToDefault(nickname, "Jogador 2"))
                            .param("token", token)
                            .param("p1", writeCards(shuffled.subList(0, deckSize)))
                            .param("p2", writeCards(shuffled.subList(deckSize, deckSize * 2)))
                            .param("code", room.code())
                            .update();
                    return view(read(room.code()), "player-two", token, false);
                });
    }

    public TrunfoRoomView get(String code, String token) {
        Room room = read(code);
        String side = sideFor(room, token);
        return view(room, side, token, false);
    }

    @Transactional
    public TrunfoRoomView leave(String code, String token) {
        Room room = read(code);
        String side = sideFor(room, token);
        String winner = "player-one".equals(side) ? room.playerTwoName() : room.playerOneName();
        jdbcClient.sql("""
                        UPDATE trunfo_room
                           SET state = 'FINISHED',
                               winner = :winner,
                               updated_at = CURRENT_TIMESTAMP
                         WHERE code = :code
                        """)
                .param("winner", winner == null ? "Adversario" : winner)
                .param("code", room.code())
                .update();
        return view(read(code), side, token, false);
    }

    @Transactional
    public TrunfoRoomView playRound(String code, String token, String attribute) {
        Room room = read(code);
        String side = sideFor(room, token);
        if (!"IN_PROGRESS".equals(room.state())) {
            throw new IllegalStateException("Partida nao esta em andamento.");
        }
        if (!side.equals(room.currentTurn())) {
            throw new IllegalArgumentException("Jogador fora da vez.");
        }
        if (!isValidAttribute(attribute)) {
            throw new IllegalArgumentException("Atributo invalido.");
        }

        List<TrunfoCard> p1 = readCards(room.playerOneDeck());
        List<TrunfoCard> p2 = readCards(room.playerTwoDeck());
        List<TrunfoCard> pile = readCards(room.disputePile());
        if (p1.isEmpty() || p2.isEmpty()) {
            throw new IllegalStateException("Partida encerrada.");
        }

        TrunfoCard c1 = p1.remove(0);
        TrunfoCard c2 = p2.remove(0);
        List<TrunfoCard> stake = new ArrayList<>(pile);
        stake.add(c1);
        stake.add(c2);

        int cmp = Double.compare(value(c1, attribute).doubleValue(), value(c2, attribute).doubleValue());
        String result = cmp > 0 ? "player" : cmp < 0 ? "cpu" : "draw";
        if ("player".equals(result)) {
            p1.addAll(stake);
            pile = List.of();
        } else if ("cpu".equals(result)) {
            p2.addAll(stake);
            pile = List.of();
        } else {
            pile = stake;
        }

        String winner = p1.isEmpty() && p2.isEmpty() ? "Empate" : p1.isEmpty() ? room.playerTwoName() : p2.isEmpty() ? room.playerOneName() : null;
        String state = winner == null ? "IN_PROGRESS" : "FINISHED";
        String nextTurn = "draw".equals(result) ? room.currentTurn() : ("player".equals(result) ? "player-one" : "player-two");
        TrunfoRoundView round = new TrunfoRoundView(room.roundNumber(), attribute, c1, c2, value(c1, attribute), value(c2, attribute), result, stake.size());
        List<TrunfoRoundView> history = new ArrayList<>(readHistory(room.history()));
        history.add(0, round);

        jdbcClient.sql("""
                        UPDATE trunfo_room
                           SET state = :state,
                               player_one_deck = :p1,
                               player_two_deck = :p2,
                               dispute_pile = :pile,
                               history = :history,
                               current_turn = :turn,
                               round_number = :round,
                               winner = :winner,
                               updated_at = CURRENT_TIMESTAMP
                         WHERE code = :code
                        """)
                .param("state", state)
                .param("p1", writeCards(p1))
                .param("p2", writeCards(p2))
                .param("pile", writeCards(pile))
                .param("history", writeHistory(history))
                .param("turn", nextTurn)
                .param("round", room.roundNumber() + 1)
                .param("winner", winner)
                .param("code", room.code())
                .update();
        return view(read(code), side, token, true);
    }

    private TrunfoRoomView view(Room room, String side, String token, boolean revealOpponent) {
        List<TrunfoCard> own = "player-one".equals(side) ? readCards(room.playerOneDeck()) : readCards(room.playerTwoDeck());
        List<TrunfoCard> other = "player-one".equals(side) ? readCards(room.playerTwoDeck()) : readCards(room.playerOneDeck());
        List<TrunfoRoundView> history = readHistory(room.history());
        return new TrunfoRoomView(
                room.code(),
                room.state(),
                side,
                token,
                room.playerOneName(),
                room.playerTwoName(),
                room.currentTurn(),
                room.roundNumber(),
                own.size(),
                other.size(),
                readCards(room.disputePile()).size(),
                own.isEmpty() ? null : own.get(0),
                revealOpponent && !other.isEmpty() ? other.get(0) : null,
                history.isEmpty() ? null : history.get(0),
                room.winner(),
                history.stream().limit(12).toList()
        );
    }

    private Room read(String code) {
        return jdbcClient.sql("SELECT * FROM trunfo_room WHERE code = :code")
                .param("code", normalizeCode(code))
                .query((rs, rowNum) -> new Room(
                        rs.getString("code"),
                        rs.getString("state"),
                        rs.getString("mode"),
                        rs.getString("difficulty"),
                        rs.getInt("deck_size"),
                        rs.getString("type_name"),
                        rs.getString("player_one_name"),
                        rs.getString("player_two_name"),
                        rs.getString("player_one_token"),
                        rs.getString("player_two_token"),
                        rs.getString("player_one_deck"),
                        rs.getString("player_two_deck"),
                        rs.getString("dispute_pile"),
                        rs.getString("history"),
                        rs.getString("current_turn"),
                        rs.getInt("round_number"),
                        rs.getString("winner")
                ))
                .optional()
                .orElseThrow(() -> new IllegalArgumentException("Sala nao encontrada."));
    }

    private String sideFor(Room room, String token) {
        if (room.playerOneToken().equals(token)) return "player-one";
        if (token != null && token.equals(room.playerTwoToken())) return "player-two";
        throw new IllegalArgumentException("Jogador nao pertence a sala.");
    }

    private boolean exists(String code) {
        return jdbcClient.sql("SELECT COUNT(*) FROM trunfo_room WHERE code = :code")
                .param("code", code)
                .query(Integer.class)
                .single() > 0;
    }

    private String randomCode() {
        return "PKM-" + (1000 + RANDOM.nextInt(9000));
    }

    private int sanitizeDeckSize(Integer deckSize) {
        return deckSize != null && deckSize >= NORMAL_DECK_SIZE ? NORMAL_DECK_SIZE : FAST_DECK_SIZE;
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    private String blankToDefault(String value, String fallback) {
        return hasText(value) ? value.trim() : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private List<TrunfoCard> readCards(String json) {
        return readList(json, CARD_LIST);
    }

    private List<TrunfoRoundView> readHistory(String json) {
        return readList(json, ROUND_LIST);
    }

    private <T> List<T> readList(String json, TypeReference<List<T>> type) {
        if (!hasText(json)) return new ArrayList<>();
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (Exception exception) {
            throw new IllegalStateException("Estado da sala invalido.", exception);
        }
    }

    private String writeCards(List<TrunfoCard> cards) {
        return write(cards);
    }

    private String writeHistory(List<TrunfoRoundView> history) {
        return write(history);
    }

    private String write(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("Estado da sala invalido.", exception);
        }
    }

    private boolean isValidAttribute(String attribute) {
        return List.of("hp", "attack", "defense", "specialAttack", "specialDefense", "speed", "weight", "height", "total").contains(attribute);
    }

    private Number value(TrunfoCard card, String attribute) {
        return switch (attribute) {
            case "hp" -> card.attributes().hp();
            case "attack" -> card.attributes().attack();
            case "defense" -> card.attributes().defense();
            case "specialAttack" -> card.attributes().specialAttack();
            case "specialDefense" -> card.attributes().specialDefense();
            case "speed" -> card.attributes().speed();
            case "weight" -> card.attributes().weight();
            case "height" -> card.attributes().height();
            case "total" -> card.attributes().total();
            default -> throw new IllegalArgumentException("Atributo invalido.");
        };
    }

    private record Room(
            String code,
            String state,
            String mode,
            String difficulty,
            Integer deckSize,
            String typeName,
            String playerOneName,
            String playerTwoName,
            String playerOneToken,
            String playerTwoToken,
            String playerOneDeck,
            String playerTwoDeck,
            String disputePile,
            String history,
            String currentTurn,
            Integer roundNumber,
            String winner
    ) {
    }
}
