package br.com.ruszel.pokedex.api.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(WebClientResponseException.class)
    public Mono<ResponseEntity<ApiError>> handleWebClient(WebClientResponseException exception, ServerWebExchange exchange) {
        log.warn("external API error status={} path={}", exception.getStatusCode(), exchange.getRequest().getPath(), exception);
        return error(HttpStatus.BAD_GATEWAY, "Falha ao consultar serviço externo.", exchange);
    }

    @ExceptionHandler(DataBufferLimitException.class)
    public Mono<ResponseEntity<ApiError>> handleBufferLimit(DataBufferLimitException exception, ServerWebExchange exchange) {
        log.warn("response too large path={}", exchange.getRequest().getPath(), exception);
        return error(HttpStatus.BAD_GATEWAY, "Resposta externa maior que o limite suportado.", exchange);
    }

    @ExceptionHandler(DataAccessException.class)
    public Mono<ResponseEntity<ApiError>> handleDatabase(DataAccessException exception, ServerWebExchange exchange) {
        log.error("database error path={}", exchange.getRequest().getPath(), exception);
        return error(HttpStatus.SERVICE_UNAVAILABLE, "Banco de dados temporariamente indisponível.", exchange);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Mono<ResponseEntity<ApiError>> handleNoResource(NoResourceFoundException exception, ServerWebExchange exchange) {
        log.warn("resource not found path={}", exchange.getRequest().getPath());
        return error(HttpStatus.NOT_FOUND, "Recurso nao encontrado.", exchange);
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiError>> handleGeneric(Exception exception, ServerWebExchange exchange) {
        log.error("unexpected API error path={}", exchange.getRequest().getPath(), exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao processar a solicitação.", exchange);
    }

    private Mono<ResponseEntity<ApiError>> error(HttpStatus status, String message, ServerWebExchange exchange) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value()
        );
        return Mono.just(ResponseEntity.status(status).body(body));
    }
}
