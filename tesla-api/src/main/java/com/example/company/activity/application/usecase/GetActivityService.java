package com.example.company.activity.application.usecase;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.example.company.activity.domain.model.ActivityRawEntry;
import com.example.company.activity.domain.model.ActivityResult;
import com.example.company.activity.domain.port.in.GetActivityUseCase;
import com.example.company.activity.domain.port.out.ActivityQueryPort;
import com.example.company.shifts.domain.exception.ShiftNotFoundException;
import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetActivityService implements GetActivityUseCase {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final ActivityQueryPort activityQueryPort;
    private final ShiftRepositoryPort shiftRepository;

    public GetActivityService(ActivityQueryPort activityQueryPort, ShiftRepositoryPort shiftRepository) {
        this.activityQueryPort = activityQueryPort;
        this.shiftRepository = shiftRepository;
    }

    @Override
    public List<ActivityResult> findByOperatorAndShift(Long operatorId, Long shiftId) {
        Shift shift = shiftRepository.findActiveById(shiftId)
                .orElseThrow(() -> new ShiftNotFoundException(shiftId));
        boolean overnight = !shift.endTime().isAfter(shift.startTime());

        List<ActivityRawEntry> all = new ArrayList<>();
        all.addAll(activityQueryPort.findReceptionsByOperatorAndTimeWindow(operatorId, shift.startTime(), shift.endTime(), overnight));
        all.addAll(activityQueryPort.findCuttingByOperatorAndShift(operatorId, shiftId));
        all.addAll(activityQueryPort.findScrapByOperatorAndShift(operatorId, shiftId));
        all.addAll(activityQueryPort.findMoldingByOperatorAndTimeWindow(operatorId, shift.startTime(), shift.endTime(), overnight));

        return all.stream()
                .sorted(Comparator.comparing(ActivityRawEntry::recordedAt))
                .map(this::toResult)
                .toList();
    }

    private ActivityResult toResult(ActivityRawEntry entry) {
        return new ActivityResult(
                entry.id(),
                TIME_FORMATTER.format(entry.recordedAt()),
                entry.containerCode(),
                entry.profileCode(),
                entry.action(),
                formatQuantities(entry),
                entry.status()
        );
    }

    private String formatQuantities(ActivityRawEntry entry) {
        return switch (entry.action()) {
            case RECEPTION -> entry.primaryQuantity() + " pcs";
            case CUT -> entry.primaryQuantity() + " → " + entry.secondaryQuantity() + " good · " + entry.tertiaryQuantity() + " scrap";
            case SCRAP -> entry.primaryQuantity() + " pcs";
            case MOLDING_OUTPUT -> entry.primaryQuantity() + " pcs";
        };
    }
}