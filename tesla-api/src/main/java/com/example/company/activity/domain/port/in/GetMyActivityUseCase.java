package com.example.company.activity.domain.port.in;

import com.example.company.activity.domain.model.ActivityItem;

import java.util.List;

public interface GetMyActivityUseCase {

    List<ActivityItem> getMyActivity(Long operatorId, Long shiftId);
}
