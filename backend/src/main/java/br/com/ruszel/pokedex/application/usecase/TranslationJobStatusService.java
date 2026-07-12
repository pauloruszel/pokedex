package br.com.ruszel.pokedex.application.usecase;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TranslationJobStatusService {
    private final JdbcClient jdbcClient;

    public void start(String jobName, int total) {
        jdbcClient.sql("""
                        MERGE INTO translation_job_status (
                            job_name, status, total, processed, failures, last_error, started_at, updated_at, finished_at
                        )
                        KEY(job_name)
                        VALUES (:jobName, 'RUNNING', :total, 0, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL)
                        """)
                .param("jobName", jobName)
                .param("total", total)
                .update();
    }

    public void progress(String jobName, int processed, int failures) {
        jdbcClient.sql("""
                        UPDATE translation_job_status
                           SET processed = :processed,
                               failures = :failures,
                               updated_at = CURRENT_TIMESTAMP
                         WHERE job_name = :jobName
                        """)
                .param("jobName", jobName)
                .param("processed", processed)
                .param("failures", failures)
                .update();
    }

    public void failOne(String jobName, int processed, int failures, String lastError) {
        jdbcClient.sql("""
                        UPDATE translation_job_status
                           SET processed = :processed,
                               failures = :failures,
                               last_error = :lastError,
                               updated_at = CURRENT_TIMESTAMP
                         WHERE job_name = :jobName
                        """)
                .param("jobName", jobName)
                .param("processed", processed)
                .param("failures", failures)
                .param("lastError", truncate(lastError))
                .update();
    }

    public void finish(String jobName, int processed, int failures) {
        String status = failures == 0 ? "DONE" : "FAILED";
        jdbcClient.sql("""
                        UPDATE translation_job_status
                           SET status = :status,
                               processed = :processed,
                               failures = :failures,
                               updated_at = CURRENT_TIMESTAMP,
                               finished_at = CURRENT_TIMESTAMP
                         WHERE job_name = :jobName
                        """)
                .param("jobName", jobName)
                .param("status", status)
                .param("processed", processed)
                .param("failures", failures)
                .update();
    }

    public Optional<TranslationJobStatus> current(String jobName) {
        return jdbcClient.sql("""
                        SELECT job_name, status, total, processed, failures, last_error, started_at, updated_at, finished_at
                          FROM translation_job_status
                         WHERE job_name = :jobName
                        """)
                .param("jobName", jobName)
                .query((rs, rowNum) -> new TranslationJobStatus(
                        rs.getString("job_name"),
                        rs.getString("status"),
                        rs.getInt("total"),
                        rs.getInt("processed"),
                        rs.getInt("failures"),
                        rs.getString("last_error"),
                        rs.getObject("started_at", Instant.class),
                        rs.getObject("updated_at", Instant.class),
                        rs.getObject("finished_at", Instant.class)
                ))
                .optional();
    }

    private String truncate(String value) {
        if (value == null || value.length() <= 1000) {
            return value;
        }
        return value.substring(0, 1000);
    }

    @Schema(description = "Status persistido do job de manutenção de traduções.")
    public record TranslationJobStatus(
            @Schema(description = "Nome do job.", example = "translation-refresh")
            String jobName,
            @Schema(description = "Estado atual do job.", example = "RUNNING")
            String status,
            @Schema(description = "Total de itens planejados.", example = "2000")
            int total,
            @Schema(description = "Quantidade já processada.", example = "120")
            int processed,
            @Schema(description = "Quantidade de falhas.", example = "3")
            int failures,
            @Schema(description = "Último erro registrado, se existir.")
            String lastError,
            @Schema(description = "Início do job.")
            Instant startedAt,
            @Schema(description = "Última atualização do job.")
            Instant updatedAt,
            @Schema(description = "Fim do job, quando concluído.")
            Instant finishedAt
    ) {
    }
}
