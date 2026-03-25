package com.atheboy.shiftpilot;

import com.atheboy.shiftpilot.repository.ShiftAssignmentRepository;
import com.atheboy.shiftpilot.service.DashboardView;
import com.atheboy.shiftpilot.service.SchedulingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SchedulingServiceIntegrationTest {

    @Autowired
    private SchedulingService schedulingService;

    @Autowired
    private ShiftAssignmentRepository assignmentRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void dashboardIncludesCoverageRisksAndTeamLoadAnalytics() {
        DashboardView dashboard = schedulingService.loadDashboard();

        assertThat(dashboard.summary().totalShifts()).isEqualTo(6);
        assertThat(dashboard.summary().openSlots()).isGreaterThan(0);
        assertThat(dashboard.teamLoads()).isNotEmpty();
        assertThat(dashboard.coverageRisks()).isNotEmpty();
    }

    @Test
    void canAssignAnEligibleEmployeeToAnOpenShift() {
        long assignmentsBefore = assignmentRepository.findByEmployeeId(6L).size();

        schedulingService.assignEmployee(5L, 6L);

        long assignmentsAfter = assignmentRepository.findByEmployeeId(6L).size();
        DashboardView updatedDashboard = schedulingService.loadDashboard();

        assertThat(assignmentsAfter).isEqualTo(assignmentsBefore + 1);
        assertThat(updatedDashboard.shifts())
                .filteredOn(shift -> shift.shiftId().equals(5L))
                .singleElement()
                .satisfies(shift -> assertThat(shift.assignedHeadcount()).isEqualTo(1));
    }

    @Test
    void recommendationApiReturnsRankedCandidates() throws Exception {
        mockMvc.perform(get("/api/shifts/5/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eligible").value(true))
                .andExpect(jsonPath("$[0].employeeName").isNotEmpty())
                .andExpect(jsonPath("$[0].score").isNumber());
    }
}
