package com.example.company.activity.domain.port.out;

import com.example.company.activity.domain.model.ActivityItem;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityQueryPort {

    List<ActivityItem> findReceptions(Long operatorId, LocalDateTime start, LocalDateTime end);

    List<ActivityItem> findCuttingRecords(Long operatorId, LocalDateTime start, LocalDateTime end);

    List<ActivityItem> findScrapRecords(Long operatorId, LocalDateTime start, LocalDateTime end);

    List<ActivityItem> findMoldingOutputs(Long operatorId, LocalDateTime start, LocalDateTime end);
}
