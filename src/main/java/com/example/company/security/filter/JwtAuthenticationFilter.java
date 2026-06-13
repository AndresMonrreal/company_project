package com.example.company.security.filter;

import java.io.IOException;

import com.example.company.security.adapter.in.web.SecurityErrorWriter;
import com.example.company.security.adapter.out.jwt.JwtRequestValidationAdapter;
import com.example.company.security.model.AuthenticatedUserPrincipal;
import com.example.company.security.model.JwtValidationException;
import com.example.company.security.model.JwtValidationResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtRequestValidationAdapter jwtRequestValidationAdapter;
    private final SecurityErrorWriter securityErrorWriter;

    public JwtAuthenticationFilter(
            JwtRequestValidationAdapter jwtRequestValidationAdapter,
            SecurityErrorWriter securityErrorWriter
    ) {
        this.jwtRequestValidationAdapter = jwtRequestValidationAdapter;
        this.securityErrorWriter = securityErrorWriter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.startsWith("/api/")
                || isPublicEndpoint(request.getMethod(), path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = resolveBearerToken(request);
            if (token == null) {
                securityErrorWriter.missingToken(request, response);
                return;
            }

            JwtValidationResult result = jwtRequestValidationAdapter.validate(token);
            AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(result.userContext());
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.authorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtValidationException ex) {
            securityErrorWriter.invalidToken(request, response, ex);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        if (!authorization.startsWith(BEARER_PREFIX)) {
            throw new JwtValidationException(JwtValidationException.Reason.INVALID_TOKEN);
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            throw new JwtValidationException(JwtValidationException.Reason.INVALID_TOKEN);
        }
        return token;
    }

    private boolean isPublicEndpoint(String method, String path) {
        return (HttpMethod.POST.matches(method) && "/api/auth/login".equals(path))
                || (HttpMethod.GET.matches(method) && "/api/health".equals(path));
    }
}
