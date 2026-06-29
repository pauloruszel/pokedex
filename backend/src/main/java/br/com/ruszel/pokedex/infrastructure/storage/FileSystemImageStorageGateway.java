package br.com.ruszel.pokedex.infrastructure.storage;

import br.com.ruszel.pokedex.application.port.ImageStorageGateway;
import br.com.ruszel.pokedex.domain.model.PokemonImage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
public class FileSystemImageStorageGateway implements ImageStorageGateway {
    private final HttpClient httpClient;
    private final Path rootPath;

    public FileSystemImageStorageGateway(
            @Value("${pokedex.images.storage-path:./data/pokedex-images}") String storagePath
    ) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.rootPath = Path.of(storagePath).toAbsolutePath().normalize();
    }

    @Override
    public PokemonImage cache(Integer pokemonId, String imageType, String sourceUrl, String publicUrl) {
        try {
            Files.createDirectories(rootPath.resolve(String.valueOf(pokemonId)));
            Path imagePath = imagePath(pokemonId, imageType, sourceUrl);

            if (!Files.exists(imagePath) || Files.size(imagePath) == 0) {
                HttpRequest request = HttpRequest.newBuilder(URI.create(sourceUrl))
                        .timeout(Duration.ofSeconds(20))
                        .GET()
                        .build();
                HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IllegalStateException("Origem retornou HTTP " + response.statusCode() + " para imagem: " + sourceUrl);
                }
                byte[] content = response.body();

                if (content == null || content.length == 0) {
                    throw new IllegalStateException("Imagem vazia retornada pela origem: " + sourceUrl);
                }
                Files.write(imagePath, content);
            }

            return new PokemonImage(
                    pokemonId,
                    imageType,
                    sourceUrl,
                    imagePath.toString(),
                    publicUrl,
                    contentType(imagePath),
                    Files.size(imagePath)
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Erro ao salvar imagem no filesystem", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Download de imagem interrompido", exception);
        }
    }

    @Override
    public Optional<PokemonImage> read(Integer pokemonId, String imageType) {
        Path pokemonFolder = rootPath.resolve(String.valueOf(pokemonId));
        if (!Files.isDirectory(pokemonFolder)) {
            return Optional.empty();
        }
        try (var files = Files.list(pokemonFolder)) {
            return files
                    .filter(path -> path.getFileName().toString().startsWith(imageType + "."))
                    .filter(Files::isRegularFile)
                    .findFirst()
                    .map(path -> toImage(pokemonId, imageType, path));
        } catch (IOException exception) {
            log.warn("Erro ao ler imagem cacheada do Pokémon {} tipo {}", pokemonId, imageType, exception);
            return Optional.empty();
        }
    }

    private PokemonImage toImage(Integer pokemonId, String imageType, Path path) {
        try {
            return new PokemonImage(
                    pokemonId,
                    imageType,
                    null,
                    path.toString(),
                    "/api/pokemon/%d/images/%s".formatted(pokemonId, imageType),
                    contentType(path),
                    Files.size(path)
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Erro ao mapear imagem cacheada", exception);
        }
    }

    private Path imagePath(Integer pokemonId, String imageType, String sourceUrl) {
        return rootPath
                .resolve(String.valueOf(pokemonId))
                .resolve(imageType + extension(sourceUrl));
    }

    private String extension(String sourceUrl) {
        String path = URI.create(sourceUrl).getPath();
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex < 0) {
            return ".png";
        }
        return path.substring(dotIndex).toLowerCase();
    }

    private String contentType(Path path) {
        try {
            String detected = Files.probeContentType(path);
            return detected == null ? "image/png" : detected;
        } catch (IOException exception) {
            return "image/png";
        }
    }
}
