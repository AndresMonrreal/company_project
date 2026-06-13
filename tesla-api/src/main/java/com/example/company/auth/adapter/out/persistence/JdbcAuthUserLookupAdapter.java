package com.example.company.auth.adapter.out.persistence;

import java.util.Optional;

import com.example.company.auth.domain.model.AuthUserRecord;
import com.example.company.auth.domain.port.out.AuthUserLookupPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcAuthUserLookupAdapter implements AuthUserLookupPort {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAuthUserLookupAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<AuthUserRecord> findByUsername(String username) {
        // Auth reads users and roles from the existing V1 schema; this login adapter does not own schema changes.
        return jdbcTemplate.query(
                """
                SELECT u.id,
                       u.username,
                       u.full_name,
                       u.password_hash,
                       u.active AS user_active,
                       r.name AS role_name,
                       r.active AS role_active
                FROM users u
                LEFT JOIN roles r ON r.id = u.role_id
                WHERE u.username = ?
                """,
                resultSet -> {
                    if (!resultSet.next()) {
                        return Optional.empty();
                    }

                    return Optional.of(new AuthUserRecord(
                            resultSet.getLong("id"),
                            resultSet.getString("username"),
                            resultSet.getString("full_name"),
                            resultSet.getString("password_hash"),
                            resultSet.getBoolean("user_active"),
                            resultSet.getString("role_name"),
                            resultSet.getObject("role_active", Boolean.class)
                    ));
                },
                username
        );
    }
}
