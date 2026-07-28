package com.example.company.activity.adapter.in.web.dto;

public record ActivityResponse(
        Long id,
        String time,
        String containerCode,
        String profileCode,
        String action,
        String quantities,
        String status,
        String lot
) {
}