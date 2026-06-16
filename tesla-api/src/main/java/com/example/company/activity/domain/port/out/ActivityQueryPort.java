package com.example.company.activity.domain.port.out;

import java.time.LocalTime;
import java.util.List;

import com.example.company.activity.domain.model.ActivityRawEntry;

public interface ActivityQueryPort {

    List<ActivityRawEntry> findReceptionsByOperatorAndTimeWindow(Long operatorId, LocalTime start, LocalTime end, boolean overnight);

    List<ActivityRawEntry> findCuttingByOperatorAndShift(Long operatorId, Long shiftId);

    List<ActivityRawEntry> findScrapByOperatorAndShift(Long operatorId, Long shiftId);

    List<ActivityRawEntry> findMoldingByOperatorAndTimeWindow(Long operatorId, LocalTime start, LocalTime end, boolean overnight);
}