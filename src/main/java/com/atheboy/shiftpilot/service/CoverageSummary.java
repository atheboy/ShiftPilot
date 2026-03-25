package com.atheboy.shiftpilot.service;

public record CoverageSummary(
        int totalShifts,
        int fullyStaffedShifts,
        int openSlots,
        int criticalOpenShifts,
        int employeeCount
) {
}
