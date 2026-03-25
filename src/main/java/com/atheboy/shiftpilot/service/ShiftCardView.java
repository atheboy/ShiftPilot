package com.atheboy.shiftpilot.service;

import com.atheboy.shiftpilot.domain.ShiftPriority;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ShiftCardView(
        Long shiftId,
        LocalDate shiftDate,
        LocalTime startTime,
        LocalTime endTime,
        String location,
        String requiredSkill,
        String shiftType,
        ShiftPriority priority,
        int requiredHeadcount,
        int assignedHeadcount,
        List<String> assignedEmployees,
        int recommendationCount
) {

    public int openSlots() {
        return Math.max(0, requiredHeadcount - assignedHeadcount);
    }

    public boolean fullyStaffed() {
        return openSlots() == 0;
    }
}
