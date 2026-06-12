package com.example.company.container_types.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.company.container_types.domain.exception.DuplicateContainerTypeNameException;
import com.example.company.container_types.domain.model.ContainerType;
import com.example.company.container_types.domain.port.in.ContainerTypeResult;
import com.example.company.container_types.domain.port.in.CreateContainerTypeCommand;
import com.example.company.container_types.domain.port.out.ContainerTypeRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateContainerTypeServiceTest {

    @Mock
    private ContainerTypeRepositoryPort containerTypeRepository;

    @InjectMocks
    private CreateContainerTypeService service;

    @Test
    void createsContainerTypeWhenNameIsUnique() {
        when(containerTypeRepository.existsByName("Rack")).thenReturn(false);
        when(containerTypeRepository.save(any(ContainerType.class)))
                .thenReturn(ContainerType.restore(1L, "Rack", true));

        ContainerTypeResult result = service.create(new CreateContainerTypeCommand("Rack"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Rack");
        assertThat(result.active()).isTrue();
        verify(containerTypeRepository).save(any(ContainerType.class));
    }

    @Test
    void rejectsDuplicateName() {
        when(containerTypeRepository.existsByName("Rack")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateContainerTypeCommand("Rack")))
                .isInstanceOf(DuplicateContainerTypeNameException.class)
                .hasMessage("Container type name already exists: Rack");
    }
}
