package com.example.company.containers.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.company.containers.domain.exception.ContainerNotFoundException;
import com.example.company.containers.domain.model.Container;
import com.example.company.containers.domain.port.out.ContainerRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteContainerServiceTest {

    @Mock
    private ContainerRepositoryPort containerRepository;

    @InjectMocks
    private DeleteContainerService service;

    @Test
    void softDeletesActiveContainer() {
        Container container = Container.restore(1L, 10L, "BOX-001", true);
        when(containerRepository.findActiveById(1L)).thenReturn(Optional.of(container));

        service.delete(1L);

        ArgumentCaptor<Container> captor = ArgumentCaptor.forClass(Container.class);
        verify(containerRepository).save(captor.capture());
        assertThat(captor.getValue().active()).isFalse();
    }

    @Test
    void rejectsMissingContainer() {
        when(containerRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ContainerNotFoundException.class)
                .hasMessage("Container not found with id: 99");
    }
}
