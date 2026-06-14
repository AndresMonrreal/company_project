package com.example.company.security.config;

import com.example.company.security.adapter.in.web.SecurityErrorWriter;
import com.example.company.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfiguration {

    private static final String[] CATALOG_COLLECTIONS = {
            "/api/profiles",
            "/api/roles",
            "/api/machines",
            "/api/shifts",
            "/api/container-types",
            "/api/containers"
    };

    private static final String[] CATALOG_ITEMS = {
            "/api/profiles/**",
            "/api/roles/**",
            "/api/machines/**",
            "/api/shifts/**",
            "/api/container-types/**",
            "/api/containers/**"
    };

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            SecurityErrorWriter securityErrorWriter,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, ex) ->
                                securityErrorWriter.missingToken(request, response))
                        .accessDeniedHandler((request, response, ex) ->
                                securityErrorWriter.forbidden(request, response))
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .requestMatchers(HttpMethod.GET, CATALOG_COLLECTIONS).hasAnyRole("ADMIN", "SUPERVISOR")
                        .requestMatchers(HttpMethod.GET, CATALOG_ITEMS).hasAnyRole("ADMIN", "SUPERVISOR")
                        .requestMatchers(HttpMethod.POST, CATALOG_COLLECTIONS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, CATALOG_ITEMS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, CATALOG_ITEMS).hasRole("ADMIN")
                        .requestMatchers("/api/**").denyAll()
                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}