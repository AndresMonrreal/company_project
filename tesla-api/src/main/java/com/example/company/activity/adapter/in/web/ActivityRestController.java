package com.example.company.activity.adapter.in.web;

import java.util.List;

import com.example.company.activity.adapter.in.web.dto.ActivityResponse;
import com.example.company.activity.domain.port.in.GetActivityUseCase;
import com.example.company.security.model.AuthenticatedUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activity")
public class ActivityRestController {

    private final GetActivityUseCase getActivity;

    public ActivityRestController(GetActivityUseCase getActivity) {
        this.getActivity = getActivity;
    }

    @GetMapping("/my")
    public List<ActivityResponse> findMy(@RequestParam Long shiftId,
                                         @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return getActivity.findByOperatorAndShift(principal.userId(), shiftId)
                .stream()
                .map(result -> new ActivityResponse(
                        result.id(),
                        result.time(),
                        result.containerCode(),
                        result.profileCode(),
                        result.action().name(),
                        result.quantities(),
                        result.status(),
                        result.lot()
                ))
                .toList();
    }
}