package com.example.company.container_types.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.company.container_types.domain.exception.ContainerTypeNotFoundException;
import com.example.company.container_types.domain.model.ContainerType;
import com.example.company.container_types.domain.port.out.ContainerTypeRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteContainerTypeServiceTest {

    @Mock
    private ContainerTypeRepositoryPort containerTypeRepository;

    @InjectMocks
    private DeleteContainerTypeService service;

    @Test
    void softDeletesActiveContainerType() {
        ContainerType containerType = ContainerType.restore(1L, "Rack", true);
        when(containerTypeRepository.findActiveById(1L)).thenReturn(Optional.of(containerType));

        service.delete(1L);

        ArgumentCaptor<ContainerType> captor = ArgumentCaptor.forClass(ContainerType.class);
        verify(containerTypeRepository).save(captor.capture());
        assertThat(captor.getValue().active()).isFalse();
    }

    @Test
    void rejectsMissingContainerType() {
        when(containerTypeRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ContainerTypeNotFoundException.class)
                .hasMessage("Container type not found with id: 99");
    }
}
