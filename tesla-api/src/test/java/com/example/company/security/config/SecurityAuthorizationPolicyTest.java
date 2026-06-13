package com.example.company.security.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class SecurityAuthorizationPolicyTest {

    private static final Set<String> CATALOG_ENDPOINTS = Set.of(
            "/api/profiles",
            "/api/roles",
            "/api/machines",
            "/api/shifts",
            "/api/container-types",
            "/api/containers"
    );

    @Test
    void currentCatalogPolicyAllowsAdminToReadAndWrite() {
        assertThat(isAllowed(HttpMethod.GET, "/api/profiles", "ADMIN")).isTrue();
        assertThat(isAllowed(HttpMethod.POST, "/api/profiles", "ADMIN")).isTrue();
        assertThat(isAllowed(HttpMethod.PUT, "/api/profiles", "ADMIN")).isTrue();
        assertThat(isAllowed(HttpMethod.DELETE, "/api/profiles", "ADMIN")).isTrue();
    }

    @Test
    void currentCatalogPolicyAllowsSupervisorToReadOnly() {
        assertThat(isAllowed(HttpMethod.GET, "/api/machines", "SUPERVISOR")).isTrue();
        assertThat(isAllowed(HttpMethod.POST, "/api/machines", "SUPERVISOR")).isFalse();
        assertThat(isAllowed(HttpMethod.PUT, "/api/machines", "SUPERVISOR")).isFalse();
        assertThat(isAllowed(HttpMethod.DELETE, "/api/machines", "SUPERVISOR")).isFalse();
    }

    @Test
    void currentCatalogPolicyBlocksOperadorAndConsulta() {
        for (String role : Set.of("OPERADOR", "CONSULTA")) {
            assertThat(isAllowed(HttpMethod.GET, "/api/containers", role)).isFalse();
            assertThat(isAllowed(HttpMethod.POST, "/api/containers", role)).isFalse();
            assertThat(isAllowed(HttpMethod.PUT, "/api/containers", role)).isFalse();
            assertThat(isAllowed(HttpMethod.DELETE, "/api/containers", role)).isFalse();
        }
    }

    @Test
    void publicEndpointPolicyKeepsLoginAndHealthOpen() {
        assertThat(isPublic(HttpMethod.POST, "/api/auth/login")).isTrue();
        assertThat(isPublic(HttpMethod.GET, "/api/health")).isTrue();
        assertThat(isPublic(HttpMethod.GET, "/api/profiles")).isFalse();
    }

    private boolean isAllowed(HttpMethod method, String endpoint, String role) {
        if (!CATALOG_ENDPOINTS.contains(endpoint)) {
            return false;
        }
        Map<HttpMethod, Set<String>> allowedRoles = Map.of(
                HttpMethod.GET, Set.of("ADMIN", "SUPERVISOR"),
                HttpMethod.POST, Set.of("ADMIN"),
                HttpMethod.PUT, Set.of("ADMIN"),
                HttpMethod.DELETE, Set.of("ADMIN")
        );
        return allowedRoles.getOrDefault(method, Set.of()).contains(role);
    }

    private boolean isPublic(HttpMethod method, String endpoint) {
        return (HttpMethod.POST.equals(method) && "/api/auth/login".equals(endpoint))
                || (HttpMethod.GET.equals(method) && "/api/health".equals(endpoint));
    }
}
