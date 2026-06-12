package com.example.company.roles.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.company.roles.domain.exception.DuplicateRoleNameException;
import com.example.company.roles.domain.exception.RoleNotFoundException;
import com.example.company.roles.domain.model.Role;
import com.example.company.roles.domain.port.in.RoleResult;
import com.example.company.roles.domain.port.in.UpdateRoleCommand;
import com.example.company.roles.domain.port.out.RoleRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateRoleServiceTest {

    @Mock
    private RoleRepositoryPort roleRepository;

    @InjectMocks
    private UpdateRoleService service;

    @Test
    void updatesRoleWhenNameIsUnique() {
        when(roleRepository.findActiveById(1L)).thenReturn(Optional.of(Role.restore(1L, "ADMIN", "Admin", true)));
        when(roleRepository.existsByNameAndIdNot("SUPERVISOR", 1L)).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(Role.restore(1L, "SUPERVISOR", "Supervisor", true));

        RoleResult result = service.update(1L, new UpdateRoleCommand("SUPERVISOR", "Supervisor"));

        assertThat(result.name()).isEqualTo("SUPERVISOR");
        assertThat(result.description()).isEqualTo("Supervisor");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void rejectsMissingRole() {
        when(roleRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new UpdateRoleCommand("SUPERVISOR", "Supervisor")))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Role not found: 99");
    }

    @Test
    void rejectsDuplicateNameForAnotherRole() {
        when(roleRepository.findActiveById(1L)).thenReturn(Optional.of(Role.restore(1L, "ADMIN", "Admin", true)));
        when(roleRepository.existsByNameAndIdNot("SUPERVISOR", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new UpdateRoleCommand("SUPERVISOR", "Supervisor")))
                .isInstanceOf(DuplicateRoleNameException.class)
                .hasMessage("Role name already exists: SUPERVISOR");
    }
}
