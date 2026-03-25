package com.atheboy.shiftpilot.service;

import com.atheboy.shiftpilot.domain.AvailabilityWindow;
import com.atheboy.shiftpilot.domain.Employee;
import com.atheboy.shiftpilot.domain.EmployeeSkill;
import com.atheboy.shiftpilot.domain.Shift;
import com.atheboy.shiftpilot.domain.ShiftAssignment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Collectors;

@Component
public class ShiftRecommendationEngine {

    private static final Duration MINIMUM_REST = Duration.ofHours(12);

    public List<CandidateRecommendation> recommend(Shift targetShift, List<Employee> employees, Map<Long, List<ShiftAssignment>> assignmentsByEmployee) {
        return employees.stream()
                .map(employee -> evaluate(employee, targetShift, assignmentsByEmployee.getOrDefault(employee.getId(), List.of())))
                .filter(CandidateRecommendation::eligible)
                .sorted(Comparator.comparingInt(CandidateRecommendation::score).reversed()
                        .thenComparing(CandidateRecommendation::employeeName))
                .toList();
    }

    public void validateAssignment(Employee employee, Shift shift, List<ShiftAssignment> assignments) {
        CandidateRecommendation recommendation = evaluate(employee, shift, assignments);
        if (!recommendation.eligible()) {
            throw new AssignmentRuleViolationException(employee.getFullName() + " is not eligible for this shift: "
                    + String.join("; ", recommendation.reasons()));
        }
    }

    private CandidateRecommendation evaluate(Employee employee, Shift targetShift, List<ShiftAssignment> assignments) {
        List<String> reasons = new ArrayList<>();
        int currentWeeklyHours = weeklyHours(assignments, targetShift);

        OptionalInt skillLevel = employee.getSkills().stream()
                .filter(skill -> skill.getSkill().getId().equals(targetShift.getRequiredSkill().getId()))
                .mapToInt(EmployeeSkill::getSkillLevel)
                .findFirst();

        if (skillLevel.isEmpty()) {
            return ineligible(employee, 0, currentWeeklyHours, "Missing required skill: " + targetShift.getRequiredSkill().getDisplayName());
        }

        if (!isAvailable(employee, targetShift)) {
            return ineligible(employee, skillLevel.getAsInt(), currentWeeklyHours, "Unavailable for " + targetShift.getShiftDate().getDayOfWeek()
                    + " " + targetShift.getStartTime() + "-" + targetShift.getEndTime());
        }

        if (hasOverlap(assignments, targetShift)) {
            return ineligible(employee, skillLevel.getAsInt(), currentWeeklyHours, "Already assigned to an overlapping shift");
        }

        if (!hasMinimumRest(assignments, targetShift)) {
            return ineligible(employee, skillLevel.getAsInt(), currentWeeklyHours, "Violates the 12 hour minimum rest policy");
        }

        int projectedWeeklyHours = currentWeeklyHours + shiftDurationHours(targetShift);
        if (projectedWeeklyHours > employee.getMaxWeeklyHours()) {
            return ineligible(employee, skillLevel.getAsInt(), projectedWeeklyHours,
                    "Would exceed max weekly hours (" + projectedWeeklyHours + "h / " + employee.getMaxWeeklyHours() + "h)");
        }

        reasons.add("Has required skill at level " + skillLevel.getAsInt());
        reasons.add("Respects 12 hour rest window");
        reasons.add("Projected weekly load: " + projectedWeeklyHours + "h / " + employee.getMaxWeeklyHours() + "h");

        int score = 50 + skillLevel.getAsInt() * 10;
        score += employee.getPreferredShiftType() == targetShift.getShiftType() ? 15 : 0;
        score += Math.max(0, employee.getMaxWeeklyHours() - projectedWeeklyHours);

        return new CandidateRecommendation(
                employee.getId(),
                employee.getFullName(),
                employee.getTeamName(),
                skillLevel.getAsInt(),
                projectedWeeklyHours,
                score,
                true,
                reasons
        );
    }

    private boolean isAvailable(Employee employee, Shift shift) {
        return employee.getAvailabilityWindows().stream().anyMatch(window ->
                window.getDayOfWeek().equals(shift.getShiftDate().getDayOfWeek())
                        && !window.getStartTime().isAfter(shift.getStartTime())
                        && !window.getEndTime().isBefore(shift.getEndTime()));
    }

    private boolean hasOverlap(List<ShiftAssignment> assignments, Shift targetShift) {
        LocalDateTime targetStart = shiftStart(targetShift);
        LocalDateTime targetEnd = shiftEnd(targetShift);
        return assignments.stream()
                .map(ShiftAssignment::getShift)
                .anyMatch(existing -> {
                    LocalDateTime existingStart = shiftStart(existing);
                    LocalDateTime existingEnd = shiftEnd(existing);
                    return targetStart.isBefore(existingEnd) && existingStart.isBefore(targetEnd);
                });
    }

    private boolean hasMinimumRest(List<ShiftAssignment> assignments, Shift targetShift) {
        LocalDateTime targetStart = shiftStart(targetShift);
        LocalDateTime targetEnd = shiftEnd(targetShift);
        return assignments.stream()
                .map(ShiftAssignment::getShift)
                .allMatch(existing -> restGapSatisfied(existing, targetStart, targetEnd));
    }

    private boolean restGapSatisfied(Shift existing, LocalDateTime targetStart, LocalDateTime targetEnd) {
        LocalDateTime existingStart = shiftStart(existing);
        LocalDateTime existingEnd = shiftEnd(existing);
        if (!targetStart.isBefore(existingEnd) && Duration.between(existingEnd, targetStart).compareTo(MINIMUM_REST) >= 0) {
            return true;
        }
        return !existingStart.isBefore(targetEnd) && Duration.between(targetEnd, existingStart).compareTo(MINIMUM_REST) >= 0;
    }

    private int weeklyHours(List<ShiftAssignment> assignments, Shift targetShift) {
        WeekFields weekFields = WeekFields.of(Locale.UK);
        int targetWeek = targetShift.getShiftDate().get(weekFields.weekOfWeekBasedYear());
        return assignments.stream()
                .map(ShiftAssignment::getShift)
                .filter(shift -> shift.getShiftDate().get(weekFields.weekOfWeekBasedYear()) == targetWeek)
                .mapToInt(this::shiftDurationHours)
                .sum();
    }

    private int shiftDurationHours(Shift shift) {
        return (int) Duration.between(shiftStart(shift), shiftEnd(shift)).toHours();
    }

    private LocalDateTime shiftStart(Shift shift) {
        return LocalDateTime.of(shift.getShiftDate(), shift.getStartTime());
    }

    private LocalDateTime shiftEnd(Shift shift) {
        LocalDateTime start = shiftStart(shift);
        LocalDateTime end = LocalDateTime.of(shift.getShiftDate(), shift.getEndTime());
        return end.isAfter(start) ? end : end.plusDays(1);
    }

    private CandidateRecommendation ineligible(Employee employee, int skillLevel, int projectedWeeklyHours, String reason) {
        return new CandidateRecommendation(
                employee.getId(),
                employee.getFullName(),
                employee.getTeamName(),
                skillLevel,
                projectedWeeklyHours,
                0,
                false,
                List.of(reason)
        );
    }

    public Map<Long, List<ShiftAssignment>> groupAssignmentsByEmployee(List<ShiftAssignment> assignments) {
        return assignments.stream().collect(Collectors.groupingBy(assignment -> assignment.getEmployee().getId()));
    }
}
