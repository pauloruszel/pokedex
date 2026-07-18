package br.com.ruszel.pokedex.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "pokedex.bootstrap.enabled=false",
        "pokedex.translation.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:flyway-migration-test;DB_CLOSE_DELAY=-1",
        "spring.flyway.enabled=true",
        "spring.sql.init.mode=never"
})
class FlywayMigrationTest {

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void createsSchemaWithFlywayOnly() {
        Integer tables = jdbcClient.sql("""
                        SELECT COUNT(*)
                          FROM information_schema.tables
                         WHERE LOWER(table_schema) = 'public'
                           AND LOWER(table_name) IN ('flyway_schema_history', 'pokemon', 'trunfo_room')
                        """)
                .query(Integer.class)
                .single();

        assertThat(tables).isEqualTo(3);
    }
}
