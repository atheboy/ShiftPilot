package com.atheboy.shiftpilot.service;

import com.atheboy.shiftpilot.domain.Employee;
import com.atheboy.shiftpilot.domain.Shift;
import com.atheboy.shiftpilot.domain.ShiftAssignment;
import com.atheboy.shiftpilot.domain.ShiftPriority;
import com.atheboy.shiftpilot.repository.EmployeeRepository;
import com.atheboy.shiftpilot.repository.ShiftAssignmentRepository;
import com.atheboy.shiftpilot.repository.ShiftRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SchedulingService {

    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftAssignmentRepository assignmentRepository;
    private final ShiftRecommendationEngine recommendationEngine;

    public SchedulingService(
            ShiftRepository shiftRepository,
            EmployeeRepository employeeRepository,
            ShiftAssignmentRepository assignmentRepository,
            ShiftRecommendationEngine recommendationEngine
    ) {
        this.shiftRepository = shiftRepository;
        this.employeeRepository = employeeRepository;
        this.assignmentRepository = assignmentRepository;
        this.recommendationEngine = recommendationEngine;
    }

    @Transactional
    public DashboardView loadDashboard() {
        List<Employee> employees = employeeRepository.findAll();
        List<ShiftAssignment> assignments = assignmentRepository.findAllByOrderByShiftShiftDateAscShiftStartTimeAsc();
        Map<Long, List<ShiftAssignment>> assignmentsByEmployee = recommendationEngine.groupAssignmentsByEmployee(assignments);
        List<Shift> shifts = shiftRepository.findAllByOrderByShiftDateAscStartTimeAsc();
        List<ShiftCardView> shiftCards = shifts.stream()
                .map(shift -> toShiftCard(shift, employees, assignmentsByEmployee))
                .toList();

        int openSlots = shiftCards.stream().mapToInt(ShiftCardView::openSlots).sum();
        int criticalOpenShifts = (int) shiftCards.stream()
                .filter(card -> !card.fullyStaffed() && card.priority() == ShiftPriority.CRITICAL)
                .count();
        int fullyStaffed = (int) shiftCards.stream().filter(ShiftCardView::fullyStaffed).count();

        CoverageSummary summary = new CoverageSummary(
                shiftCards.size(),
                fullyStaffed,
                openSlots,
                criticalOpenShifts,
                employees.size()
        );
        return new DashboardView(summary, buildTeamLoads(employees, assignments), buildCoverageRisks(shifts, employees, assignmentsByEmployee), shiftCards);
    }

    @Transactional
    public void assignEmployee(Long shiftId, Long employeeId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new AssignmentRuleViolationException("Shift not found."));
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AssignmentRuleViolationException("Employee not found."));

        if (shift.getAssignments().stream().anyMatch(assignment -> assignment.getEmployee().getId().equals(employeeId))) {
            throw new AssignmentRuleViolationException(employee.getFullName() + " is already assigned to that shift.");
        }

        if (shift.getAssignments().size() >= shift.getRequiredHeadcount()) {
            throw new AssignmentRuleViolationException("That shift is already fully staffed.");
        }

        recommendationEngine.validateAssignment(employee, shift, assignmentRepository.findByEmployeeId(employeeId));
        ShiftAssignment assignment = new ShiftAssignment(shift, employee, Instant.now());
        shift.getAssignments().add(assignment);
        assignmentRepository.save(assignment);
    }

    @Transactional
    public List<CandidateRecommendation> recommendationsForShift(Long shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new AssignmentRuleViolationException("Shift not found."));
        List<Employee> employees = employeeRepository.findAll();
        Map<Long, List<ShiftAssignment>> assignmentsByEmployee = recommendationEngine.groupAssignmentsByEmployee(
                assignmentRepository.findAllByOrderByShiftShiftDateAscShiftStartTimeAsc()
        );
        return recommendationEngine.recommend(shift, employees, assignmentsByEmployee);
    }

    private ShiftCardView toShiftCard(Shift shift, List<Employee> employees, Map<Long, List<ShiftAssignment>> assignmentsByEmployee) {
        List<String> assignedEmployees = shift.getAssignments().stream()
                .map(assignment -> assignment.getEmployee().getFullName())
                .sorted()
                .toList();
        int recommendationCount = recommendationEngine.recommend(shift, employees, assignmentsByEmployee).size();

        return new ShiftCardView(
                shift.getId(),
                shift.getShiftDate(),
                shift.getStartTime(),
                shift.getEndTime(),
                shift.getLocation(),
                shift.getRequiredSkill().getDisplayName(),
                shift.getShiftType().name(),
                shift.getPriority(),
                shift.getRequiredHeadcount(),
                shift.getAssignments().size(),
                assignedEmployees,
                recommendationCount
        );
    }

    private List<TeamLoadView> buildTeamLoads(List<Employee> employees, List<ShiftAssignment> assignments) {
        Map<String, Integer> capacityByTeam = employees.stream()
                .collect(Collectors.groupingBy(Employee::getTeamName, Collectors.summingInt(Employee::getMaxWeeklyHours)));

        Map<String, Integer> assignedHoursByTeam = assignments.stream()
                .collect(Collectors.groupingBy(assignment -> assignment.getEmployee().getTeamName(),
                        Collectors.summingInt(assignment -> shiftDurationHours(assignment.getShift()))));

        Map<String, Long> assignedShiftsByTeam = assignments.stream()
                .collect(Collectors.groupingBy(assignment -> assignment.getEmployee().getTeamName(), Collectors.counting()));

        return capacityByTeam.entrySet().stream()
                .map(entry -> {
                    String teamName = entry.getKey();
                    int capacityHours = entry.getValue();
                    int assignedHours = assignedHoursByTeam.getOrDefault(teamName, 0);
                    int utilizationPercent = capacityHours == 0 ? 0 : Math.round((assignedHours * 100f) / capacityHours);
                    return new TeamLoadView(
                            teamName,
                            assignedHours,
                            capacityHours,
                            assignedShiftsByTeam.getOrDefault(teamName, 0L).intValue(),
                            utilizationPercent
                    );
                })
                .sorted((left, right) -> Integer.compare(right.utilizationPercent(), left.utilizationPercent()))
                .toList();
    }

    private List<CoverageRiskView> buildCoverageRisks(List<Shift> shifts, List<Employee> employees, Map<Long, List<ShiftAssignment>> assignmentsByEmployee) {
        return shifts.stream()
                .filter(shift -> shift.getAssignments().size() < shift.getRequiredHeadcount())
                .map(shift -> {
                    List<CandidateRecommendation> recommendations = recommendationEngine.recommend(shift, employees, assignmentsByEmployee);
                    String severity = shift.getPriority() == ShiftPriority.CRITICAL ? "critical"
                            : shift.getPriority() == ShiftPriority.HIGH ? "high" : "medium";
                    String title = recommendations.isEmpty()
                            ? "No eligible backup for " + shift.getRequiredSkill().getDisplayName()
                            : shift.getRequiredSkill().getDisplayName() + " still has " + (shift.getRequiredHeadcount() - shift.getAssignments().size()) + " open slot(s)";
                    String detail = recommendations.isEmpty()
                            ? "No employee currently passes skill, availability, overlap, rest, and weekly-hour checks for "
                            + shift.getLocation() + " on " + formatWindow(shift) + "."
                            : recommendations.size() + " eligible candidate(s) remain for " + shift.getLocation() + " on " + formatWindow(shift) + ".";
                    return new CoverageRiskView(shift.getId(), severity, title, detail);
                })
                .sorted((left, right) -> severityRank(left.severity()) - severityRank(right.severity()))
                .limit(5)
                .toList();
    }

    private int severityRank(String severity) {
        return switch (severity) {
            case "critical" -> 0;
            case "high" -> 1;
            default -> 2;
        };
    }

    private String formatWindow(Shift shift) {
        return shift.getShiftDate() + " " + shift.getStartTime() + "-" + shift.getEndTime();
    }

    private int shiftDurationHours(Shift shift) {
        LocalDateTime start = LocalDateTime.of(shift.getShiftDate(), shift.getStartTime());
        LocalDateTime end = LocalDateTime.of(shift.getShiftDate(), shift.getEndTime());
        if (!end.isAfter(start)) {
            end = end.plusDays(1);
        }
        return (int) java.time.Duration.between(start, end).toHours();
    }
}
