package com.example.company.container_types.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.example.company.container_types.domain.exception.ContainerTypeNotFoundException;
import com.example.company.container_types.domain.model.ContainerType;
import com.example.company.container_types.domain.port.in.ContainerTypeResult;
import com.example.company.container_types.domain.port.out.ContainerTypeRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetContainerTypeServiceTest {

    @Mock
    private ContainerTypeRepositoryPort containerTypeRepository;

    @InjectMocks
    private GetContainerTypeService service;

    @Test
    void listsActiveContainerTypes() {
        when(containerTypeRepository.findAllActiveOrderByNameAsc()).thenReturn(List.of(
                ContainerType.restore(1L, "Bin", true),
                ContainerType.restore(2L, "Rack", true)
        ));

        List<ContainerTypeResult> results = service.findAllActive();

        assertThat(results).extracting(ContainerTypeResult::name).containsExactly("Bin", "Rack");
    }

    @Test
    void findsContainerTypeById() {
        when(containerTypeRepository.findActiveById(1L))
                .thenReturn(Optional.of(ContainerType.restore(1L, "Rack", true)));

        ContainerTypeResult result = service.findById(1L);

        assertThat(result.name()).isEqualTo("Rack");
    }

    @Test
    void rejectsMissingContainerType() {
        when(containerTypeRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ContainerTypeNotFoundException.class)
                .hasMessage("Container type not found with id: 99");
    }
}
