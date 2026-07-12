package br.com.ruszel.pokedex.api.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class AdminApiAccessPolicy {
    private static final String ADMIN_PREFIX = "/api/admin/";
    private static final String ADMIN_TOKEN_HEADER = "X-Admin-Token";

    @Value("${pokedex.admin.enabled:true}")
    private boolean enabled;

    @Value("${pokedex.admin.token:local-dev-token}")
    private String adminToken;

    public boolean isAdminPath(String path) {
        return path != null && path.startsWith(ADMIN_PREFIX);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAuthorized(HttpHeaders headers) {
        String providedToken = headers.getFirst(ADMIN_TOKEN_HEADER);
        String bearerToken = bearerToken(headers);
        return constantEquals(adminToken, providedToken) || constantEquals(adminToken, bearerToken);
    }

    private String bearerToken(HttpHeaders headers) {
        String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring("Bearer ".length());
    }

    private boolean constantEquals(String expected, String actual) {
        if (expected == null || expected.isBlank() || actual == null) {
            return false;
        }
        if (expected.length() != actual.length()) {
            return false;
        }
        int result = 0;
        for (int index = 0; index < expected.length(); index++) {
            result |= expected.charAt(index) ^ actual.charAt(index);
        }
        return result == 0;
    }
}
