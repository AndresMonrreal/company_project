package com.example.company.roles.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.company.roles.domain.exception.RoleNotFoundException;
import com.example.company.roles.domain.model.Role;
import com.example.company.roles.domain.port.out.RoleRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteRoleServiceTest {

    @Mock
    private RoleRepositoryPort roleRepository;

    @InjectMocks
    private DeleteRoleService service;

    @Test
    void softDeletesRole() {
        when(roleRepository.findActiveById(1L)).thenReturn(Optional.of(Role.restore(1L, "ADMIN", "Admin", true)));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.delete(1L);

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());
        assertThat(captor.getValue().active()).isFalse();
    }

    @Test
    void rejectsMissingRole() {
        when(roleRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Role not found: 99");
    }
}
