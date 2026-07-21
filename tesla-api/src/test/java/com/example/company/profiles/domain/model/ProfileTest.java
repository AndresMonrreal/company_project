package com.example.company.profiles.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ProfileTest {

    @Test
    void createsActiveProfileWithTypeAndPosition() {
        Profile profile = Profile.create("EXTE0001", "Profile A", null, "HEADER", "FRONT");

        assertThat(profile.id()).isNull();
        assertThat(profile.code()).isEqualTo("EXTE0001");
        assertThat(profile.name()).isEqualTo("Profile A");
        assertThat(profile.active()).isTrue();
        assertThat(profile.type()).isEqualTo("HEADER");
        assertThat(profile.position()).isEqualTo("FRONT");
    }

    @Test
    void updatesNameTypeAndPosition() {
        Profile profile = Profile.restore(1L, "EXTE0001", "Old Name", null, true, "HEADER", "FRONT");

        profile.update("New Name", null, "LOWER", "REAR");

        assertThat(profile.name()).isEqualTo("New Name");
        assertThat(profile.type()).isEqualTo("LOWER");
        assertThat(profile.position()).isEqualTo("REAR");
    }

    @Test
    void rejectsBlankCode() {
        assertThatThrownBy(() -> Profile.create(" ", "Profile A", null, "HEADER", "FRONT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Profile code is required");
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Profile.create("EXTE0001", " ", null, "HEADER", "FRONT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Profile name is required");
    }
}