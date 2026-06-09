package com.example.company.profiles.controller;

import com.example.company.profiles.service.ProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.company.profiles.dto.ProfileResponse;
import java.util.List;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public List<ProfileResponse> findAllActive() {
        return profileService.findAllActive();
    }

    @GetMapping("/{id}")
    public ProfileResponse findById(@PathVariable Long id) {
        return profileService.findById(id);
    }
}
