package com.atheboy.shiftpilot.service;

import java.util.List;

public record CandidateRecommendation(
        Long employeeId,
        String employeeName,
        String teamName,
        int skillLevel,
        int projectedWeeklyHours,
        int score,
        boolean eligible,
        List<String> reasons
) {
}
