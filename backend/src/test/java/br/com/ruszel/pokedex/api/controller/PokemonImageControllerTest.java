package br.com.ruszel.pokedex.api.controller;

import br.com.ruszel.pokedex.application.usecase.GetPokemonImageUseCase;
import br.com.ruszel.pokedex.domain.model.PokemonImage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PokemonImageControllerTest {
    @TempDir
    Path directory;

    @Test
    void servesColdCacheImageWithoutBlockingRequestEventLoop() throws Exception {
        Path file = Files.write(directory.resolve("image.png"), new byte[]{1, 2, 3});
        var useCase = mock(GetPokemonImageUseCase.class);
        when(useCase.execute(25, "official-artwork")).thenAnswer(invocation -> {
            assertThat(Schedulers.isInNonBlockingThread()).isFalse();
            return Optional.of(new PokemonImage(25, "official-artwork", null,
                    file.toString(), "/api/pokemon/25/images/official-artwork", "image/png", 3L));
        });
        var controller = new PokemonImageController(useCase);
        var response = Mono.defer(() -> controller.image(25, "official-artwork"))
                .subscribeOn(Schedulers.parallel());

        verifyNoInteractions(useCase);
        StepVerifier.create(response)
                .assertNext(result -> {
                    assertThat(result.getStatusCode().value()).isEqualTo(200);
                    assertThat(result.getHeaders().getCacheControl()).contains("max-age=604800");
                    assertThat(result.getBody().getFile()).isEqualTo(file.toFile());
                })
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void missingImageStillReturnsNotFound() {
        var useCase = mock(GetPokemonImageUseCase.class);
        when(useCase.execute(25, "official-artwork")).thenReturn(Optional.empty());
        StepVerifier.create(new PokemonImageController(useCase).image(25, "official-artwork"))
                .assertNext(result -> assertThat(result.getStatusCode().value()).isEqualTo(404))
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }
}
