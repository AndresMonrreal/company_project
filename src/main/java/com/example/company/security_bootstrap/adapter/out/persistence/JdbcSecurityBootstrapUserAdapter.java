package com.example.company.security_bootstrap.adapter.out.persistence;

import com.example.company.security_bootstrap.domain.model.BootstrapUserDefinition;
import com.example.company.security_bootstrap.domain.port.out.SecurityBootstrapUserPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSecurityBootstrapUserAdapter implements SecurityBootstrapUserPort {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSecurityBootstrapUserAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean existsByUsername(String username) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM users WHERE username = ?)",
                Boolean.class,
                username
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void insertBootstrapUser(BootstrapUserDefinition user, Long roleId, String passwordHash) {
        jdbcTemplate.update(
                """
                INSERT INTO users (role_id, full_name, username, password_hash, active)
                VALUES (?, ?, ?, ?, TRUE)
                ON CONFLICT (username) DO NOTHING
                """,
                roleId,
                user.fullName(),
                user.username(),
                passwordHash
        );
    }
}
