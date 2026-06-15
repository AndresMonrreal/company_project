package com.example.company.activity.application.usecase;

import com.example.company.activity.domain.model.ActivityItem;
import com.example.company.activity.domain.port.in.GetMyActivityUseCase;
import com.example.company.activity.domain.port.out.ActivityQueryPort;
import com.example.company.activity.domain.port.out.ShiftWindowPort;
import com.example.company.activity.domain.port.out.ShiftWindowPort.ShiftWindow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class GetMyActivityService implements GetMyActivityUseCase {

    private final ActivityQueryPort activityQueryPort;
    private final ShiftWindowPort shiftWindowPort;

    public GetMyActivityService(ActivityQueryPort activityQueryPort, ShiftWindowPort shiftWindowPort) {
        this.activityQueryPort = activityQueryPort;
        this.shiftWindowPort = shiftWindowPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityItem> getMyActivity(Long operatorId, Long shiftId) {
        ShiftWindow window = shiftWindowPort.findWindowByShiftId(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + shiftId));

        LocalDateTime windowStart = LocalDate.now().atTime(window.startTime());

        LocalDateTime windowEnd;
        if (window.endTime().isAfter(window.startTime())) {
            windowEnd = LocalDate.now().atTime(window.endTime());
        } else {
            windowEnd = LocalDate.now().plusDays(1).atTime(window.endTime());
        }

        List<ActivityItem> all = new ArrayList<>();
        all.addAll(activityQueryPort.findReceptions(operatorId, windowStart, windowEnd));
        all.addAll(activityQueryPort.findCuttingRecords(operatorId, windowStart, windowEnd));
        all.addAll(activityQueryPort.findScrapRecords(operatorId, windowStart, windowEnd));
        all.addAll(activityQueryPort.findMoldingOutputs(operatorId, windowStart, windowEnd));

        all.sort(Comparator.comparing(ActivityItem::timestamp));

        return all;
    }
}