package com.example.company.activity.adapter.in.web;

import com.example.company.activity.adapter.in.web.dto.ActivityResponse;
import com.example.company.activity.domain.port.in.GetMyActivityUseCase;
import com.example.company.security.model.AuthenticatedUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/activity")
public class ActivityRestController {

    private final GetMyActivityUseCase getMyActivity;

    public ActivityRestController(GetMyActivityUseCase getMyActivity) {
        this.getMyActivity = getMyActivity;
    }

    @GetMapping("/my")
    public List<ActivityResponse> getMyActivity(
            @RequestParam Long shiftId,
            Principal principal
    ) {
        AuthenticatedUserPrincipal auth = (AuthenticatedUserPrincipal) ((Authentication) principal).getPrincipal();
        return getMyActivity.getMyActivity(auth.userId(), shiftId)
                .stream()
                .map(item -> new ActivityResponse(
                        item.id(),
                        item.time(),
                        item.containerCode(),
                        item.profileCode(),
                        item.action(),
                        item.quantities(),
                        item.status()
                ))
                .toList();
    }
}