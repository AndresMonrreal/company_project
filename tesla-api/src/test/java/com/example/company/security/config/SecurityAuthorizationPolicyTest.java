package com.example.company.security.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Prueba de integración real contra SecurityConfiguration. Cada caso hace una
 * petición HTTP real vía MockMvc y verifica la respuesta del SecurityFilterChain
 * real. Si SecurityConfiguration.java cambia sin actualizar este test, el test falla.
 *
 * IDs en rutas con {id} son intencionalmente inexistentes (999999) para no
 * mutar datos reales de la BD de desarrollo.
 */
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
class SecurityAuthorizationPolicyTest {

    private static final Set<String> ALL_ROLES = Set.of("ADMIN", "SUPERVISOR", "OPERADOR", "CONSULTA");
    private static final String EMPTY_JSON_BODY = "{}";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private record EndpointRule(HttpMethod method, String path, Set<String> allowedRoles) {
        Set<String> deniedRoles() {
            return ALL_ROLES.stream()
                    .filter(role -> !allowedRoles.contains(role))
                    .collect(Collectors.toSet());
        }
    }

    private static List<EndpointRule> endpointRules() {
        Set<String> catalogRead = Set.of("ADMIN", "SUPERVISOR", "OPERADOR");
        Set<String> adminOnly = Set.of("ADMIN");
        Set<String> writeRoles = Set.of("ADMIN", "SUPERVISOR");
        Set<String> anyRole = ALL_ROLES;

        return List.of(
                new EndpointRule(HttpMethod.GET, "/api/profiles", catalogRead),
                new EndpointRule(HttpMethod.POST, "/api/profiles", adminOnly),
                new EndpointRule(HttpMethod.PUT, "/api/profiles/999999", adminOnly),
                new EndpointRule(HttpMethod.DELETE, "/api/profiles/999999", adminOnly),
                new EndpointRule(HttpMethod.GET, "/api/machines", catalogRead),
                new EndpointRule(HttpMethod.GET, "/api/shifts", catalogRead),
                new EndpointRule(HttpMethod.GET, "/api/containers", catalogRead),
                new EndpointRule(HttpMethod.GET, "/api/container-types", catalogRead),
                new EndpointRule(HttpMethod.GET, "/api/roles", catalogRead),
                new EndpointRule(HttpMethod.POST, "/api/receptions", writeRoles),
                new EndpointRule(HttpMethod.GET, "/api/receptions/my", anyRole),
                new EndpointRule(HttpMethod.POST, "/api/cutting", writeRoles),
                new EndpointRule(HttpMethod.GET, "/api/cutting/available", anyRole),
                new EndpointRule(HttpMethod.POST, "/api/scrap", writeRoles),
                new EndpointRule(HttpMethod.GET, "/api/scrap/my", anyRole),
                new EndpointRule(HttpMethod.POST, "/api/molding-outputs", writeRoles),
                new EndpointRule(HttpMethod.GET, "/api/molding-outputs/my", anyRole),
                new EndpointRule(HttpMethod.GET, "/api/activity/my", anyRole),
                new EndpointRule(HttpMethod.GET, "/api/inventory/available", anyRole),
                new EndpointRule(HttpMethod.GET, "/api/shifts/current", anyRole)
        );
    }

    @ParameterizedTest(name = "{0} {1} deja pasar a los roles autorizados")
    @MethodSource("endpointRules")
    void allowedRolesPassSecurityLayer(EndpointRule rule) throws Exception {
        for (String role : rule.allowedRoles()) {
            int status = mockMvc.perform(request(rule.method(), rule.path())
                            .with(user("tester").roles(role))
                            .contentType("application/json")
                            .content(EMPTY_JSON_BODY))
                    .andReturn().getResponse().getStatus();

            assertThat(status)
                    .as("%s %s con rol %s no debería ser bloqueado por seguridad, fue %d",
                            rule.method(), rule.path(), role, status)
                    .isNotIn(401, 403);
        }
    }

    @ParameterizedTest(name = "{0} {1} bloquea con 403 a los roles NO autorizados")
    @MethodSource("endpointRules")
    void deniedRolesGetForbidden(EndpointRule rule) throws Exception {
        for (String role : rule.deniedRoles()) {
            mockMvc.perform(request(rule.method(), rule.path())
                            .with(user("tester").roles(role))
                            .contentType("application/json")
                            .content(EMPTY_JSON_BODY))
                    .andExpect(status().isForbidden());
        }
    }

    @ParameterizedTest(name = "{0} {1} bloquea con 401 sin token")
    @MethodSource("endpointRules")
    void unauthenticatedGetsUnauthorized(EndpointRule rule) throws Exception {
        mockMvc.perform(request(rule.method(), rule.path())
                        .contentType("application/json")
                        .content(EMPTY_JSON_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginIsPublicRegardlessOfCredentials() throws Exception {
        int status = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"nadie\",\"password\":\"nada\"}"))
                .andReturn().getResponse().getStatus();

        assertThat(status).isNotEqualTo(403);
    }

    @Test
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/api/health")).andExpect(status().isOk());
    }

    @Test
    void unknownApiPathIsForbiddenForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/this-does-not-exist").with(user("tester").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void unknownApiPathIsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/this-does-not-exist"))
                .andExpect(status().isUnauthorized());
    }
}
