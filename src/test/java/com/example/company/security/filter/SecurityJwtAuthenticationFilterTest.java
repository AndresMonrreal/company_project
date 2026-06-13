package com.example.company.security.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.example.company.auth.adapter.out.security.JwtProperties;
import com.example.company.security.adapter.in.web.SecurityErrorWriter;
import com.example.company.security.adapter.out.jwt.JwtRequestValidationAdapter;
import com.example.company.security.model.AuthenticatedUserContext;
import com.example.company.security.model.AuthenticatedUserPrincipal;
import com.example.company.security.model.JwtValidationException;
import com.example.company.security.model.JwtValidationResult;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityJwtAuthenticationFilterTest {

    @Test
    void publicEndpointSkipsTokenValidationEvenWhenAuthorizationHeaderIsInvalid() throws Exception {
        CapturingFilterChain chain = new CapturingFilterChain();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(unusedValidationAdapter(), new SecurityErrorWriter());
        MockHttpServletRequest request = request("POST", "/api/auth/login");
        request.addHeader(HttpHeaders.AUTHORIZATION, "not-bearer");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(chain.called()).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void protectedEndpointWithoutTokenReturnsSafeUnauthorizedResponse() throws Exception {
        CapturingFilterChain chain = new CapturingFilterChain();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(unusedValidationAdapter(), new SecurityErrorWriter());
        MockHttpServletRequest request = request("GET", "/api/profiles");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(chain.called()).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString())
                .contains("security.missing-token")
                .doesNotContain("password")
                .doesNotContain("secret")
                .doesNotContain("stackTrace");
    }

    @Test
    void protectedEndpointWithMalformedTokenReturnsSafeUnauthorizedResponse() throws Exception {
        CapturingFilterChain chain = new CapturingFilterChain();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                validationAdapter(token -> {
                    throw new JwtValidationException(JwtValidationException.Reason.INVALID_TOKEN);
                }),
                new SecurityErrorWriter()
        );
        MockHttpServletRequest request = request("GET", "/api/profiles");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer malformed");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(chain.called()).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString())
                .contains("security.invalid-token")
                .doesNotContain("malformed")
                .doesNotContain("parser");
    }

    @Test
    void validTokenCreatesAuthenticationForDownstreamFiltersAndClearsContextAfterRequest() throws Exception {
        CapturingFilterChain chain = new CapturingFilterChain();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                validationAdapter(token -> new JwtValidationResult(new AuthenticatedUserContext(
                        1L,
                        "admin",
                        "ADMIN",
                        Instant.parse("2026-06-12T18:30:00Z")
                ))),
                new SecurityErrorWriter()
        );
        MockHttpServletRequest request = request("GET", "/api/profiles");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(chain.called()).isTrue();
        Authentication authentication = chain.authentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isInstanceOf(AuthenticatedUserPrincipal.class);
        AuthenticatedUserPrincipal principal = (AuthenticatedUserPrincipal) authentication.getPrincipal();
        assertThat(principal.userId()).isEqualTo(1L);
        assertThat(principal.username()).isEqualTo("admin");
        assertThat(principal.role()).isEqualTo("ADMIN");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private JwtRequestValidationAdapter unusedValidationAdapter() {
        return validationAdapter(token -> {
            throw new AssertionError("JWT validation should not run");
        });
    }

    private JwtRequestValidationAdapter validationAdapter(Function<String, JwtValidationResult> validator) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-jwt-secret-value-that-is-not-real");
        return new JwtRequestValidationAdapter(properties) {
            @Override
            public JwtValidationResult validate(String token) {
                return validator.apply(token);
            }
        };
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return request;
    }

    private static final class CapturingFilterChain implements jakarta.servlet.FilterChain {

        private boolean called;
        private final AtomicReference<Authentication> authentication = new AtomicReference<>();

        @Override
        public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response)
                throws IOException, ServletException {
            called = true;
            authentication.set(SecurityContextHolder.getContext().getAuthentication());
        }

        boolean called() {
            return called;
        }

        Authentication authentication() {
            return authentication.get();
        }
    }
}
