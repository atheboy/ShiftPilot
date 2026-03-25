package com.atheboy.shiftpilot.service;

public record TeamLoadView(
        String teamName,
        int assignedHours,
        int capacityHours,
        int assignedShifts,
        int utilizationPercent
) {
}
