package com.example.company.container_types.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ContainerTypeTest {

    @Test
    void createsActiveContainerTypeWithTrimmedName() {
        ContainerType containerType = ContainerType.create("  Rack  ");

        assertThat(containerType.id()).isNull();
        assertThat(containerType.name()).isEqualTo("Rack");
        assertThat(containerType.active()).isTrue();
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> ContainerType.create(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Container type name is required");
    }

    @Test
    void softDeletesContainerType() {
        ContainerType containerType = ContainerType.restore(1L, "Rack", true);

        containerType.deactivate();

        assertThat(containerType.active()).isFalse();
    }
}
