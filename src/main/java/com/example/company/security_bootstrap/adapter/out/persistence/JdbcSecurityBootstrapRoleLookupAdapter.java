package com.example.company.security_bootstrap.adapter.out.persistence;

import java.util.Optional;

import com.example.company.security_bootstrap.domain.model.BootstrapRoleName;
import com.example.company.security_bootstrap.domain.port.out.SecurityBootstrapRoleLookupPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSecurityBootstrapRoleLookupAdapter implements SecurityBootstrapRoleLookupPort {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSecurityBootstrapRoleLookupAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Long> findRoleIdByName(BootstrapRoleName roleName) {
        return jdbcTemplate.query(
                "SELECT id FROM roles WHERE name = ? AND active = TRUE",
                (resultSet, rowNumber) -> resultSet.getLong("id"),
                roleName.value()
        ).stream().findFirst();
    }
}
