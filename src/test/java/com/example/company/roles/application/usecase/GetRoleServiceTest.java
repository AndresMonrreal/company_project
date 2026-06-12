package com.example.company.roles.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.example.company.roles.domain.exception.RoleNotFoundException;
import com.example.company.roles.domain.model.Role;
import com.example.company.roles.domain.port.in.RoleResult;
import com.example.company.roles.domain.port.out.RoleRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetRoleServiceTest {

    @Mock
    private RoleRepositoryPort roleRepository;

    @InjectMocks
    private GetRoleService service;

    @Test
    void listsActiveRoles() {
        when(roleRepository.findAllActiveOrderByNameAsc()).thenReturn(List.of(
                Role.restore(1L, "ADMIN", "Admin", true),
                Role.restore(2L, "SUPERVISOR", "Supervisor", true)
        ));

        List<RoleResult> results = service.findAllActive();

        assertThat(results).extracting(RoleResult::name).containsExactly("ADMIN", "SUPERVISOR");
    }

    @Test
    void findsRoleById() {
        when(roleRepository.findActiveById(1L)).thenReturn(Optional.of(Role.restore(1L, "ADMIN", "Admin", true)));

        RoleResult result = service.findById(1L);

        assertThat(result.name()).isEqualTo("ADMIN");
    }

    @Test
    void rejectsMissingRole() {
        when(roleRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Role not found: 99");
    }
}
