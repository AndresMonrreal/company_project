package com.example.company.machines.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MachineTest {

    @Test
    void createsActiveMachineWithTrimmedName() {
        Machine machine = Machine.create("  CUT-01  ");

        assertThat(machine.id()).isNull();
        assertThat(machine.name()).isEqualTo("CUT-01");
        assertThat(machine.active()).isTrue();
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Machine.create(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Machine name is required");
    }

    @Test
    void rejectsTooLongName() {
        String name = "A".repeat(81);

        assertThatThrownBy(() -> Machine.create(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Machine name must be 80 characters or fewer");
    }

    @Test
    void softDeletesMachine() {
        Machine machine = Machine.restore(1L, "CUT-01", true);

        machine.deactivate();

        assertThat(machine.active()).isFalse();
    }
}
