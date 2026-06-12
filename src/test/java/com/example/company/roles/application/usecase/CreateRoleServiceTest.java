package com.example.company.roles.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.company.roles.domain.exception.DuplicateRoleNameException;
import com.example.company.roles.domain.model.Role;
import com.example.company.roles.domain.port.in.CreateRoleCommand;
import com.example.company.roles.domain.port.in.RoleResult;
import com.example.company.roles.domain.port.out.RoleRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateRoleServiceTest {

    @Mock
    private RoleRepositoryPort roleRepository;

    @InjectMocks
    private CreateRoleService service;

    @Test
    void createsRoleWhenNameIsUnique() {
        when(roleRepository.existsByName("ADMIN")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(Role.restore(1L, "ADMIN", "System administrator", true));

        RoleResult result = service.create(new CreateRoleCommand("ADMIN", "System administrator"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("ADMIN");
        assertThat(result.description()).isEqualTo("System administrator");
        assertThat(result.active()).isTrue();
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void rejectsDuplicateName() {
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateRoleCommand("ADMIN", "System administrator")))
                .isInstanceOf(DuplicateRoleNameException.class)
                .hasMessage("Role name already exists: ADMIN");
    }
}
