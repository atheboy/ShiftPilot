package com.atheboy.shiftpilot.service;

import com.atheboy.shiftpilot.domain.Employee;
import com.atheboy.shiftpilot.domain.Shift;
import com.atheboy.shiftpilot.domain.ShiftAssignment;
import com.atheboy.shiftpilot.domain.ShiftPriority;
import com.atheboy.shiftpilot.domain.ShiftType;
import com.atheboy.shiftpilot.domain.Skill;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShiftRecommendationEngineTest {

    private final ShiftRecommendationEngine engine = new ShiftRecommendationEngine();

    @Test
    void ranksEligibleCandidatesByScoreAndPreference() {
        Skill incidentResponse = skill(1L, "INCIDENT_RESPONSE", "Incident Response");
        Shift targetShift = shift(11L, LocalDate.of(2026, 3, 30), LocalTime.of(9, 0), LocalTime.of(17, 0), ShiftType.DAY, incidentResponse, 1);

        Employee bestMatch = employee(1L, "Ava Collins", "Core Ops", 40, ShiftType.DAY);
        bestMatch.addSkill(incidentResponse, 5);
        bestMatch.addAvailability(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0));

        Employee backup = employee(2L, "Jonas Berg", "Security", 40, ShiftType.SWING);
        backup.addSkill(incidentResponse, 3);
        backup.addAvailability(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0));

        List<CandidateRecommendation> recommendations = engine.recommend(targetShift, List.of(bestMatch, backup), Map.of());

        assertThat(recommendations).hasSize(2);
        assertThat(recommendations.getFirst().employeeName()).isEqualTo("Ava Collins");
        assertThat(recommendations.getFirst().eligible()).isTrue();
        assertThat(recommendations.getFirst().reasons()).anyMatch(reason -> reason.contains("level 5"));
        assertThat(recommendations.getFirst().score()).isGreaterThan(recommendations.get(1).score());
    }

    @Test
    void rejectsEmployeesWhoBreakMinimumRestRules() {
        Skill incidentResponse = skill(1L, "INCIDENT_RESPONSE", "Incident Response");
        Employee employee = employee(3L, "Ethan Silva", "Response", 40, ShiftType.NIGHT);
        employee.addSkill(incidentResponse, 4);
        employee.addAvailability(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(23, 0));

        Shift existingShift = shift(20L, LocalDate.of(2026, 3, 30), LocalTime.of(9, 0), LocalTime.of(17, 0), ShiftType.DAY, incidentResponse, 1);
        Shift targetShift = shift(21L, LocalDate.of(2026, 3, 30), LocalTime.of(22, 0), LocalTime.of(23, 59), ShiftType.NIGHT, incidentResponse, 1);

        List<ShiftAssignment> assignments = List.of(new ShiftAssignment(existingShift, employee, Instant.now()));

        assertThat(engine.recommend(targetShift, List.of(employee), Map.of(employee.getId(), assignments))).isEmpty();
        assertThatThrownBy(() -> engine.validateAssignment(employee, targetShift, assignments))
                .isInstanceOf(AssignmentRuleViolationException.class)
                .hasMessageContaining("12 hour minimum rest");
    }

    @Test
    void rejectsEmployeesWhoWouldExceedWeeklyCapacity() {
        Skill platformOps = skill(2L, "PLATFORM_OPS", "Platform Operations");
        Employee employee = employee(4L, "Maya Hassan", "Platform", 32, ShiftType.DAY);
        employee.addSkill(platformOps, 5);
        employee.addAvailability(DayOfWeek.MONDAY, LocalTime.of(7, 0), LocalTime.of(19, 0));
        employee.addAvailability(DayOfWeek.TUESDAY, LocalTime.of(7, 0), LocalTime.of(19, 0));
        employee.addAvailability(DayOfWeek.WEDNESDAY, LocalTime.of(7, 0), LocalTime.of(19, 0));
        employee.addAvailability(DayOfWeek.THURSDAY, LocalTime.of(7, 0), LocalTime.of(19, 0));
        employee.addAvailability(DayOfWeek.FRIDAY, LocalTime.of(7, 0), LocalTime.of(19, 0));

        List<ShiftAssignment> assignments = List.of(
                new ShiftAssignment(shift(31L, LocalDate.of(2026, 3, 30), LocalTime.of(8, 0), LocalTime.of(16, 0), ShiftType.DAY, platformOps, 1), employee, Instant.now()),
                new ShiftAssignment(shift(32L, LocalDate.of(2026, 3, 31), LocalTime.of(8, 0), LocalTime.of(16, 0), ShiftType.DAY, platformOps, 1), employee, Instant.now()),
                new ShiftAssignment(shift(33L, LocalDate.of(2026, 4, 1), LocalTime.of(8, 0), LocalTime.of(16, 0), ShiftType.DAY, platformOps, 1), employee, Instant.now()),
                new ShiftAssignment(shift(34L, LocalDate.of(2026, 4, 2), LocalTime.of(8, 0), LocalTime.of(16, 0), ShiftType.DAY, platformOps, 1), employee, Instant.now())
        );

        Shift targetShift = shift(35L, LocalDate.of(2026, 4, 3), LocalTime.of(8, 0), LocalTime.of(16, 0), ShiftType.DAY, platformOps, 1);

        assertThat(engine.recommend(targetShift, List.of(employee), Map.of(employee.getId(), assignments))).isEmpty();
        assertThatThrownBy(() -> engine.validateAssignment(employee, targetShift, assignments))
                .isInstanceOf(AssignmentRuleViolationException.class)
                .hasMessageContaining("max weekly hours");
    }

    private static Skill skill(Long id, String code, String displayName) {
        Skill skill = new Skill(code, displayName);
        ReflectionTestUtils.setField(skill, "id", id);
        return skill;
    }

    private static Employee employee(Long id, String fullName, String teamName, int maxWeeklyHours, ShiftType preferredShiftType) {
        Employee employee = new Employee(fullName, teamName, maxWeeklyHours, preferredShiftType);
        ReflectionTestUtils.setField(employee, "id", id);
        return employee;
    }

    private static Shift shift(Long id, LocalDate date, LocalTime start, LocalTime end, ShiftType shiftType, Skill requiredSkill, int requiredHeadcount) {
        Shift shift = new Shift(date, start, end, shiftType, ShiftPriority.HIGH, "Remote", requiredSkill, requiredHeadcount, "Test shift");
        ReflectionTestUtils.setField(shift, "id", id);
        return shift;
    }
}
