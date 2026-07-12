package br.com.ruszel.pokedex.application.usecase;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class TranslationCacheKeyFactory {
    public String cacheKey(String kind, String sourceLocale, String targetLocale, String sourceText) {
        return hash(kind + "|" + sourceLocale + "|" + targetLocale + "|" + sourceText);
    }

    public String localCacheKey(String locale, String kind, String sourceText) {
        return hash(locale + "|" + kind + "|" + sourceText);
    }

    public String textHash(String sourceText) {
        return hash(sourceText);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required for translation cache keys", exception);
        }
    }
}
