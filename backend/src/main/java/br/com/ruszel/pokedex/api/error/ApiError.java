package br.com.ruszel.pokedex.api.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Resposta padrão de erro da API.")
public record ApiError(
        @Schema(description = "Instante em que o erro ocorreu.", example = "2026-07-12T03:00:00Z")
        Instant timestamp,
        @Schema(description = "Status HTTP.", example = "404")
        int status,
        @Schema(description = "Nome resumido do erro.", example = "Not Found")
        String error,
        @Schema(description = "Mensagem detalhada.", example = "Pokémon não encontrado.")
        String message,
        @Schema(description = "Path da requisição.", example = "/api/pokemon/999999")
        String path
) {
}
