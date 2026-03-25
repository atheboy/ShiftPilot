package com.atheboy.shiftpilot.service;

public record CoverageRiskView(
        Long shiftId,
        String severity,
        String title,
        String detail
) {
}
