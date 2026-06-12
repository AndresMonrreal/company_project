package com.example.company.containers.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.example.company.containers.domain.exception.ContainerNotFoundException;
import com.example.company.containers.domain.model.Container;
import com.example.company.containers.domain.port.in.ContainerResult;
import com.example.company.containers.domain.port.out.ContainerRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetContainerServiceTest {

    @Mock
    private ContainerRepositoryPort containerRepository;

    @InjectMocks
    private GetContainerService service;

    @Test
    void listsActiveContainers() {
        when(containerRepository.findAllActiveOrderByCodeAsc()).thenReturn(List.of(
                Container.restore(1L, 10L, "BOX-001", true),
                Container.restore(2L, 10L, "BOX-002", true)
        ));

        List<ContainerResult> results = service.findAllActive();

        assertThat(results).extracting(ContainerResult::code).containsExactly("BOX-001", "BOX-002");
    }

    @Test
    void findsContainerById() {
        when(containerRepository.findActiveById(1L))
                .thenReturn(Optional.of(Container.restore(1L, 10L, "BOX-001", true)));

        ContainerResult result = service.findById(1L);

        assertThat(result.code()).isEqualTo("BOX-001");
        assertThat(result.containerTypeId()).isEqualTo(10L);
    }

    @Test
    void rejectsMissingContainer() {
        when(containerRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ContainerNotFoundException.class)
                .hasMessage("Container not found with id: 99");
    }
}
