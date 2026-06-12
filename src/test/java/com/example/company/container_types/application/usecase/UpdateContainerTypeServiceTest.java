package com.example.company.container_types.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.company.container_types.domain.exception.ContainerTypeNotFoundException;
import com.example.company.container_types.domain.exception.DuplicateContainerTypeNameException;
import com.example.company.container_types.domain.model.ContainerType;
import com.example.company.container_types.domain.port.in.ContainerTypeResult;
import com.example.company.container_types.domain.port.in.UpdateContainerTypeCommand;
import com.example.company.container_types.domain.port.out.ContainerTypeRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateContainerTypeServiceTest {

    @Mock
    private ContainerTypeRepositoryPort containerTypeRepository;

    @InjectMocks
    private UpdateContainerTypeService service;

    @Test
    void updatesContainerTypeWhenNameIsUnique() {
        when(containerTypeRepository.findActiveById(1L))
                .thenReturn(Optional.of(ContainerType.restore(1L, "Rack", true)));
        when(containerTypeRepository.existsByNameAndIdNot("Bin", 1L)).thenReturn(false);
        when(containerTypeRepository.save(any(ContainerType.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ContainerTypeResult result = service.update(1L, new UpdateContainerTypeCommand("Bin"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Bin");
        assertThat(result.active()).isTrue();
        verify(containerTypeRepository).save(any(ContainerType.class));
    }

    @Test
    void rejectsMissingContainerType() {
        when(containerTypeRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new UpdateContainerTypeCommand("Bin")))
                .isInstanceOf(ContainerTypeNotFoundException.class)
                .hasMessage("Container type not found with id: 99");
    }

    @Test
    void rejectsDuplicateName() {
        when(containerTypeRepository.findActiveById(1L))
                .thenReturn(Optional.of(ContainerType.restore(1L, "Rack", true)));
        when(containerTypeRepository.existsByNameAndIdNot("Bin", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new UpdateContainerTypeCommand("Bin")))
                .isInstanceOf(DuplicateContainerTypeNameException.class)
                .hasMessage("Container type name already exists: Bin");
    }
}
