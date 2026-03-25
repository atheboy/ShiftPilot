package com.atheboy.shiftpilot.service;

import java.util.List;

public record DashboardView(
        CoverageSummary summary,
        List<TeamLoadView> teamLoads,
        List<CoverageRiskView> coverageRisks,
        List<ShiftCardView> shifts
) {
}
