package com.example.company.profiles.domain.port.in;

import java.util.List;

public interface GetProfileUseCase {

    List<ProfileResult> findAllActive();

    ProfileResult findById(Long id);
}
