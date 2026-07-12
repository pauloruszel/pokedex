package br.com.ruszel.pokedex.api.controller;

import br.com.ruszel.pokedex.config.OpenApiConfig;
import br.com.ruszel.pokedex.application.usecase.TranslationMaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/translations")
@RequiredArgsConstructor
@Tag(name = "Admin - Traduções", description = "Operações administrativas para monitorar, recarregar e limpar traduções.")
@SecurityRequirement(name = OpenApiConfig.ADMIN_TOKEN_SCHEME)
public class TranslationMaintenanceController {
    private final TranslationMaintenanceService translationMaintenanceService;

    @GetMapping("/missing")
    @Operation(
            summary = "Lista traduções pendentes",
            description = "Retorna Pokémon sem flavor text pt-BR válido no cache atual."
    )
    public List<TranslationMaintenanceService.MissingTranslation> missing(
            @Parameter(description = "Quantidade máxima de pendências retornadas.", example = "2000")
            @RequestParam(defaultValue = "2000") int limit
    ) {
        return translationMaintenanceService.findMissingFlavorTexts(limit);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Reprocessa traduções pendentes",
            description = "Busca textos originais, traduz para pt-BR, salva no cache e atualiza o status persistido do job."
    )
    public TranslationMaintenanceService.RefreshResult refresh(
            @Parameter(description = "Quantidade máxima de itens processados.", example = "2000")
            @RequestParam(defaultValue = "2000") int limit
    ) {
        return translationMaintenanceService.refreshMissingFlavorTexts(limit);
    }

    @GetMapping("/status")
    @Operation(summary = "Consulta status do job de tradução", description = "Retorna o último status persistido da manutenção de traduções.")
    public Object status() {
        return translationMaintenanceService.refreshStatus();
    }

    @PostMapping("/cleanup-invalid-cache")
    @Operation(
            summary = "Remove traduções inválidas do cache",
            description = "Apaga entradas de cache que não devem ser reutilizadas, como traduções vazias ou equivalentes ao texto original."
    )
    public TranslationMaintenanceService.CleanupResult cleanupInvalidCache() {
        return translationMaintenanceService.cleanupInvalidCache();
    }
}
