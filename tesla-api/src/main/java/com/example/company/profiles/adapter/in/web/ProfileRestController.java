package com.example.company.profiles.adapter.in.web;

import java.util.List;

import com.example.company.profiles.adapter.in.web.dto.ProfileCreateRequest;
import com.example.company.profiles.adapter.in.web.dto.ProfileResponse;
import com.example.company.profiles.adapter.in.web.dto.ProfileUpdateRequest;
import com.example.company.profiles.domain.port.in.CreateProfileUseCase;
import com.example.company.profiles.domain.port.in.DeleteProfileUseCase;
import com.example.company.profiles.domain.port.in.GetProfileUseCase;
import com.example.company.profiles.domain.port.in.UpdateProfileUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
public class ProfileRestController {

    private final CreateProfileUseCase createProfile;
    private final GetProfileUseCase getProfile;
    private final UpdateProfileUseCase updateProfile;
    private final DeleteProfileUseCase deleteProfile;
    private final ProfileWebMapper mapper;

    public ProfileRestController(
            CreateProfileUseCase createProfile,
            GetProfileUseCase getProfile,
            UpdateProfileUseCase updateProfile,
            DeleteProfileUseCase deleteProfile,
            ProfileWebMapper mapper
    ) {
        this.createProfile = createProfile;
        this.getProfile = getProfile;
        this.updateProfile = updateProfile;
        this.deleteProfile = deleteProfile;
        this.mapper = mapper;
    }

    @GetMapping
    public List<ProfileResponse> findAllActive() {
        return getProfile.findAllActive()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ProfileResponse findById(@PathVariable Long id) {
        return mapper.toResponse(getProfile.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse create(@Valid @RequestBody ProfileCreateRequest request) {
        return mapper.toResponse(createProfile.create(mapper.toCommand(request)));
    }

    @PutMapping("/{id}")
    public ProfileResponse update(@PathVariable Long id, @Valid @RequestBody ProfileUpdateRequest request) {
        return mapper.toResponse(updateProfile.update(id, mapper.toCommand(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deleteProfile.delete(id);
    }
}
