package com.example.company.profiles.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.company.profiles.domain.exception.DuplicateProfileCodeException;
import com.example.company.profiles.domain.model.Profile;
import com.example.company.profiles.domain.port.in.CreateProfileCommand;
import com.example.company.profiles.domain.port.in.ProfileResult;
import com.example.company.profiles.domain.port.out.ProfileRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateProfileServiceTest {

    @Mock
    private ProfileRepositoryPort profileRepository;

    @InjectMocks
    private CreateProfileService service;

    @Test
    void createsProfileWithTypeAndPosition() {
        when(profileRepository.existsByCode("EXTE0001")).thenReturn(false);
        when(profileRepository.save(any(Profile.class))).thenReturn(
                Profile.restore(1L, "EXTE0001", "Profile A", null, true, "HEADER", "FRONT")
        );

        ProfileResult result = service.create(new CreateProfileCommand(
                "EXTE0001", "Profile A", null, "HEADER", "FRONT"
        ));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.code()).isEqualTo("EXTE0001");
        assertThat(result.type()).isEqualTo("HEADER");
        assertThat(result.position()).isEqualTo("FRONT");
        assertThat(result.active()).isTrue();
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void rejectsDuplicateCode() {
        when(profileRepository.existsByCode("EXTE0001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateProfileCommand(
                "EXTE0001", "Profile A", null, "HEADER", "FRONT"
        )))
                .isInstanceOf(DuplicateProfileCodeException.class)
                .hasMessage("Profile code already exists: EXTE0001");
    }
}