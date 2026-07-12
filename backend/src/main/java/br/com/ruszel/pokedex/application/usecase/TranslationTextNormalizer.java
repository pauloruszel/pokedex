package br.com.ruszel.pokedex.application.usecase;

import org.springframework.stereotype.Component;

@Component
public class TranslationTextNormalizer {
    public String normalizeText(String text) {
        return text == null ? "" : text.replace("\n", " ").replace("\f", " ").replace("  ", " ").trim();
    }

    public String normalizeLocale(String locale, String fallback) {
        if (locale == null || locale.isBlank()) {
            return fallback;
        }
        String normalized = locale.trim().toLowerCase();
        if (normalized.startsWith("pt")) {
            return "pt-BR";
        }
        if (normalized.startsWith("es")) {
            return "es";
        }
        if (normalized.startsWith("en")) {
            return "en";
        }
        throw new IllegalArgumentException("Unsupported locale: " + locale);
    }

    public String providerLanguage(String locale) {
        return locale.equals("pt-BR") ? "pt" : locale;
    }

    public String normalizeKind(String kind) {
        if (kind == null || kind.isBlank()) {
            return "ui_text";
        }
        return normalizeToken(kind);
    }

    public String normalizeEntity(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return normalizeToken(value);
    }

    private String normalizeToken(String value) {
        return value.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "_");
    }
}
