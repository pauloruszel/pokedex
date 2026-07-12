package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.application.port.PokemonDetailRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationMaintenanceService {
    public static final String REFRESH_JOB_NAME = "translation-refresh";

    private final TranslationMaintenanceQueries translationMaintenanceQueries;
    private final PokemonDetailRepository pokemonRepository;
    private final TranslationJobStatusService translationJobStatusService;
    private final TranslationCacheService translationCacheService;

    public List<MissingTranslation> findMissingFlavorTexts(int limit) {
        return translationMaintenanceQueries.findMissingFlavorTexts(limit);
    }

    public RefreshResult refreshMissingFlavorTexts(int limit) {
        List<MissingTranslation> missing = findMissingFlavorTexts(limit);
        List<MissingTranslation> refreshed = new ArrayList<>();
        List<MissingTranslation> failed = new ArrayList<>();
        translationJobStatusService.start(REFRESH_JOB_NAME, missing.size());

        for (MissingTranslation pokemon : missing) {
            try {
                pokemonRepository.findByNameOrId(String.valueOf(pokemon.id()))
                        .block(Duration.ofSeconds(45));

                if (translationMaintenanceQueries.hasCurrentFlavorText(pokemon.id())) {
                    refreshed.add(pokemon);
                } else {
                    failed.add(pokemon);
                    translationJobStatusService.failOne(REFRESH_JOB_NAME, refreshed.size() + failed.size(), failed.size(), "Translation was not persisted for " + pokemon.name());
                }
            } catch (Exception exception) {
                log.warn("translation_refresh_failed pokemonId={} name={}", pokemon.id(), pokemon.name(), exception);
                failed.add(pokemon);
                translationJobStatusService.failOne(REFRESH_JOB_NAME, refreshed.size() + failed.size(), failed.size(), exception.getMessage());
            }
            translationJobStatusService.progress(REFRESH_JOB_NAME, refreshed.size() + failed.size(), failed.size());
        }

        List<MissingTranslation> remaining = findMissingFlavorTexts(limit);
        translationJobStatusService.finish(REFRESH_JOB_NAME, refreshed.size() + failed.size(), failed.size());
        return new RefreshResult(missing, refreshed, failed, remaining);
    }

    public TranslationJobStatusService.TranslationJobStatus refreshStatus() {
        return translationJobStatusService.current(REFRESH_JOB_NAME)
                .orElse(new TranslationJobStatusService.TranslationJobStatus(REFRESH_JOB_NAME, "IDLE", 0, 0, 0, null, null, null, null));
    }

    public CleanupResult cleanupInvalidCache() {
        return new CleanupResult(translationCacheService.cleanupInvalidCachedTranslations());
    }

    @Schema(description = "Pokémon com tradução pt-BR pendente.")
    public record MissingTranslation(
            @Schema(description = "ID nacional do Pokémon.", example = "25")
            int id,
            @Schema(description = "Nome canônico do Pokémon.", example = "pikachu")
            String name
    ) {
    }

    @Schema(description = "Resultado da manutenção de traduções.")
    public record RefreshResult(
            @Schema(description = "Itens solicitados para processamento.")
            List<MissingTranslation> requested,
            @Schema(description = "Itens atualizados com sucesso.")
            List<MissingTranslation> refreshed,
            @Schema(description = "Itens que falharam durante o processamento.")
            List<MissingTranslation> failed,
            @Schema(description = "Itens que continuam pendentes após a execução.")
            List<MissingTranslation> remaining
    ) {
    }

    @Schema(description = "Resultado da limpeza de cache de traduções inválidas.")
    public record CleanupResult(
            @Schema(description = "Quantidade de registros removidos.", example = "12")
            int deleted
    ) {
    }
}
