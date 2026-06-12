package com.example.company.containers.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.company.containers.domain.exception.ContainerNotFoundException;
import com.example.company.containers.domain.exception.DuplicateContainerCodeException;
import com.example.company.containers.domain.model.Container;
import com.example.company.containers.domain.port.in.ContainerResult;
import com.example.company.containers.domain.port.in.UpdateContainerCommand;
import com.example.company.containers.domain.port.out.ContainerRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateContainerServiceTest {

    @Mock
    private ContainerRepositoryPort containerRepository;

    @InjectMocks
    private UpdateContainerService service;

    @Test
    void updatesContainerWhenCodeIsUnique() {
        when(containerRepository.findActiveById(1L))
                .thenReturn(Optional.of(Container.restore(1L, 10L, "BOX-001", true)));
        when(containerRepository.existsByCodeAndIdNot("BOX-002", 1L)).thenReturn(false);
        when(containerRepository.save(any(Container.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContainerResult result = service.update(1L, new UpdateContainerCommand(20L, "BOX-002"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.containerTypeId()).isEqualTo(20L);
        assertThat(result.code()).isEqualTo("BOX-002");
        assertThat(result.active()).isTrue();
        verify(containerRepository).save(any(Container.class));
    }

    @Test
    void rejectsMissingContainer() {
        when(containerRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new UpdateContainerCommand(20L, "BOX-002")))
                .isInstanceOf(ContainerNotFoundException.class)
                .hasMessage("Container not found with id: 99");
    }

    @Test
    void rejectsDuplicateCode() {
        when(containerRepository.findActiveById(1L))
                .thenReturn(Optional.of(Container.restore(1L, 10L, "BOX-001", true)));
        when(containerRepository.existsByCodeAndIdNot("BOX-002", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new UpdateContainerCommand(20L, "BOX-002")))
                .isInstanceOf(DuplicateContainerCodeException.class)
                .hasMessage("Container code already exists: BOX-002");
    }
}
