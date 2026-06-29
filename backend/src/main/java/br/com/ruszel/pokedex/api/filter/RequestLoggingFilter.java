package br.com.ruszel.pokedex.api.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RequestLoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (!path.startsWith("/api/")) {
            return chain.filter(exchange);
        }

        long started = System.nanoTime();
        return chain.filter(exchange)
                .doFinally(signal -> {
                    long elapsedMs = (System.nanoTime() - started) / 1_000_000;
                    Integer status = exchange.getResponse().getStatusCode() == null
                            ? null
                            : exchange.getResponse().getStatusCode().value();
                    log.info("api_request method={} path={} status={} durationMs={}",
                            exchange.getRequest().getMethod(),
                            path,
                            status,
                            elapsedMs);
                });
    }
}
