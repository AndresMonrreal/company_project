package com.example.company.containers.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.company.containers.domain.exception.DuplicateContainerCodeException;
import com.example.company.containers.domain.model.Container;
import com.example.company.containers.domain.port.in.ContainerResult;
import com.example.company.containers.domain.port.in.CreateContainerCommand;
import com.example.company.containers.domain.port.out.ContainerRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateContainerServiceTest {

    @Mock
    private ContainerRepositoryPort containerRepository;

    @InjectMocks
    private CreateContainerService service;

    @Test
    void createsContainerWhenCodeIsUnique() {
        when(containerRepository.existsByCode("BOX-001")).thenReturn(false);
        when(containerRepository.save(any(Container.class)))
                .thenReturn(Container.restore(1L, 10L, "BOX-001", true));

        ContainerResult result = service.create(new CreateContainerCommand(10L, "BOX-001"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.containerTypeId()).isEqualTo(10L);
        assertThat(result.code()).isEqualTo("BOX-001");
        assertThat(result.active()).isTrue();
        verify(containerRepository).save(any(Container.class));
    }

    @Test
    void rejectsDuplicateCode() {
        when(containerRepository.existsByCode("BOX-001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateContainerCommand(10L, "BOX-001")))
                .isInstanceOf(DuplicateContainerCodeException.class)
                .hasMessage("Container code already exists: BOX-001");
    }
}
