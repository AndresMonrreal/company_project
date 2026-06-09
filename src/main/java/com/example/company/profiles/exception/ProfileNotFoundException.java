package com.example.company.profiles.exception;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(Long id) {
        super("Profile not found with id: "+ id);
    }
}
