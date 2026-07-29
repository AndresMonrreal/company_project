package com.example.company.machines.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MachineTest {

    @Test
    void createsActiveMachineWithTrimmedName() {
        Machine machine = Machine.create("  CUT-01  ", null, "OPERATIONAL", null, null, null, null);

        assertThat(machine.id()).isNull();
        assertThat(machine.name()).isEqualTo("CUT-01");
        assertThat(machine.active()).isTrue();
    }

    @Test
    void createsWithAllNewFields() {
        Machine machine = Machine.create("CUT-01", "M001", "MAINTENANCE", "HEADER", 30.0, null, "obs");

        assertThat(machine.code()).isEqualTo("M001");
        assertThat(machine.status()).isEqualTo("MAINTENANCE");
        assertThat(machine.processesType()).isEqualTo("HEADER");
        assertThat(machine.cycleTimeSeconds()).isEqualTo(30.0);
        assertThat(machine.observations()).isEqualTo("obs");
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Machine.create(" ", null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Machine name is required");
    }

    @Test
    void rejectsTooLongName() {
        String name = "A".repeat(81);

        assertThatThrownBy(() -> Machine.create(name, null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Machine name must be 80 characters or fewer");
    }

    @Test
    void softDeletesMachine() {
        Machine machine = Machine.restore(1L, "CUT-01", true, null, null, null, null, null, null);

        machine.deactivate();

        assertThat(machine.active()).isFalse();
    }
}