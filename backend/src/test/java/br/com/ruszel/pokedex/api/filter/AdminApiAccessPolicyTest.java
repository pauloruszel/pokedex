package br.com.ruszel.pokedex.api.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class AdminApiAccessPolicyTest {
    private AdminApiAccessPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new AdminApiAccessPolicy();
        ReflectionTestUtils.setField(policy, "enabled", true);
        ReflectionTestUtils.setField(policy, "adminToken", "test-token");
    }

    @Test
    void matchesOnlyAdminApiPaths() {
        assertThat(policy.isAdminPath("/api/admin/translations/missing")).isTrue();
        assertThat(policy.isAdminPath("/api/pokemon")).isFalse();
    }

    @Test
    void acceptsAdminHeaderOrBearerToken() {
        HttpHeaders headerToken = new HttpHeaders();
        headerToken.set("X-Admin-Token", "test-token");

        HttpHeaders bearerToken = new HttpHeaders();
        bearerToken.setBearerAuth("test-token");

        assertThat(policy.isAuthorized(headerToken)).isTrue();
        assertThat(policy.isAuthorized(bearerToken)).isTrue();
    }

    @Test
    void rejectsMissingOrWrongToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Admin-Token", "wrong");

        assertThat(policy.isAuthorized(new HttpHeaders())).isFalse();
        assertThat(policy.isAuthorized(headers)).isFalse();
    }
}
