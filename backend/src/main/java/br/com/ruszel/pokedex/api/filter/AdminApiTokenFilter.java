package br.com.ruszel.pokedex.api.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
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
    private final AdminApiAccessPolicy adminApiAccessPolicy;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        if (!adminApiAccessPolicy.isAdminPath(path)) {
            return chain.filter(exchange);
        }

        if (!adminApiAccessPolicy.isEnabled()) {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        }

        if (adminApiAccessPolicy.isAuthorized(exchange.getRequest().getHeaders())) {
            return chain.filter(exchange);
        }

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
