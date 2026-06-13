package com.example.company.containers.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ContainerTest {

    @Test
    void createsActiveContainerWithContainerTypeIdAndTrimmedCode() {
        Container container = Container.create(10L, "  BOX-001  ");

        assertThat(container.id()).isNull();
        assertThat(container.containerTypeId()).isEqualTo(10L);
        assertThat(container.code()).isEqualTo("BOX-001");
        assertThat(container.active()).isTrue();
    }

    @Test
    void rejectsMissingContainerTypeId() {
        assertThatThrownBy(() -> Container.create(null, "BOX-001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Container type id is required");
    }

    @Test
    void rejectsBlankCode() {
        assertThatThrownBy(() -> Container.create(10L, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Container code is required");
    }

    @Test
    void softDeletesContainer() {
        Container container = Container.restore(1L, 10L, "BOX-001", true);

        container.deactivate();

        assertThat(container.active()).isFalse();
    }
}
