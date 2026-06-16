package com.example.company.activity.domain.model;

public record ActivityResult(
        Long id,
        String time,
        String containerCode,
        String profileCode,
        ActivityAction action,
        String quantities,
        String status
) {
}