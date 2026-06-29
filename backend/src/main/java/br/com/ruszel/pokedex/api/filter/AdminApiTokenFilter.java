package br.com.ruszel.pokedex.api.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(-100)
@RequiredArgsConstructor
public class AdminApiTokenFilter implements WebFilter {
    private static final String ADMIN_PREFIX = "/api/admin/";
    private static final String ADMIN_TOKEN_HEADER = "X-Admin-Token";

    @Value("${pokedex.admin.enabled:true}")
    private boolean enabled;

    @Value("${pokedex.admin.token:local-dev-token}")
    private String adminToken;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        if (!path.startsWith(ADMIN_PREFIX)) {
            return chain.filter(exchange);
        }

        if (!enabled) {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        }

        String providedToken = exchange.getRequest().getHeaders().getFirst(ADMIN_TOKEN_HEADER);
        String bearerToken = bearerToken(exchange.getRequest().getHeaders());
        if (constantEquals(adminToken, providedToken) || constantEquals(adminToken, bearerToken)) {
            return chain.filter(exchange);
        }

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
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
