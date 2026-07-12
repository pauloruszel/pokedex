package br.com.ruszel.pokedex.api.controller;

import br.com.ruszel.pokedex.application.usecase.GetPokemonImageUseCase;
import br.com.ruszel.pokedex.domain.model.PokemonImage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/pokemon")
@RequiredArgsConstructor
@Tag(name = "Pokémon - Imagens", description = "Entrega imagens cacheadas pelo backend.")
public class PokemonImageController {
    private final GetPokemonImageUseCase getPokemonImageUseCase;

    @GetMapping("/{pokemonId}/images/{imageType}")
    @Operation(
            summary = "Obtém imagem de um Pokémon",
            description = "Retorna uma imagem armazenada no cache local. Se ainda não existir localmente, o backend tenta baixar e persistir a imagem."
    )
    @ApiResponse(responseCode = "200", description = "Imagem retornada", content = @Content(mediaType = "image/png"))
    @ApiResponse(responseCode = "404", description = "Imagem não encontrada", content = @Content)
    public ResponseEntity<FileSystemResource> image(
            @Parameter(description = "ID numérico do Pokémon.", example = "25")
            @PathVariable Integer pokemonId,
            @Parameter(description = "Tipo da imagem solicitada.", example = "official-artwork")
            @PathVariable String imageType
    ) {
        return getPokemonImageUseCase.execute(pokemonId, imageType)
                .filter(image -> image.localPath() != null && Files.exists(Path.of(image.localPath())))
                .map(this::toResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponseEntity<FileSystemResource> toResponse(PokemonImage image) {
        FileSystemResource resource = new FileSystemResource(image.localPath());
        String contentType = image.contentType() == null ? MediaType.IMAGE_PNG_VALUE : image.contentType();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=604800, immutable")
                .body(resource);
    }
}
