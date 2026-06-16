package com.example.company.activity.domain.port.in;

import java.util.List;

import com.example.company.activity.domain.model.ActivityResult;

public interface GetActivityUseCase {

    List<ActivityResult> findByOperatorAndShift(Long operatorId, Long shiftId);
}