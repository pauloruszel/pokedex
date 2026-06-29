package br.com.ruszel.pokedex.api.controller;

import br.com.ruszel.pokedex.application.usecase.GetPokemonImageUseCase;
import br.com.ruszel.pokedex.domain.model.PokemonImage;
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
public class PokemonImageController {
    private final GetPokemonImageUseCase getPokemonImageUseCase;

    @GetMapping("/{pokemonId}/images/{imageType}")
    public ResponseEntity<FileSystemResource> image(
            @PathVariable Integer pokemonId,
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
