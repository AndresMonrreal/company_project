package com.example.company.profiles.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.company.profiles.domain.exception.ProfileNotFoundException;
import com.example.company.profiles.domain.model.Profile;
import com.example.company.profiles.domain.port.in.ProfileResult;
import com.example.company.profiles.domain.port.in.UpdateProfileCommand;
import com.example.company.profiles.domain.port.out.ProfileRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateProfileServiceTest {

    @Mock
    private ProfileRepositoryPort profileRepository;

    @InjectMocks
    private UpdateProfileService service;

    @Test
    void updatesProfileNameTypeAndPosition() {
        when(profileRepository.findActiveById(1L)).thenReturn(Optional.of(
                Profile.restore(1L, "EXTE0001", "Old Name", null, true, "HEADER", "FRONT")
        ));
        when(profileRepository.save(any(Profile.class))).thenReturn(
                Profile.restore(1L, "EXTE0001", "New Name", null, true, "LOWER", "REAR")
        );

        ProfileResult result = service.update(1L, new UpdateProfileCommand(
                "New Name", null, "LOWER", "REAR"
        ));

        assertThat(result.name()).isEqualTo("New Name");
        assertThat(result.type()).isEqualTo("LOWER");
        assertThat(result.position()).isEqualTo("REAR");
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void rejectsMissingProfile() {
        when(profileRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new UpdateProfileCommand(
                "New Name", null, "LOWER", "REAR"
        )))
                .isInstanceOf(ProfileNotFoundException.class)
                .hasMessage("Profile not found with id: 99");
    }
}