package com.atheboy.shiftpilot.web;

import com.atheboy.shiftpilot.service.SchedulingService;
import com.atheboy.shiftpilot.service.AssignmentRuleViolationException;
import com.atheboy.shiftpilot.service.DashboardView;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DashboardController {
    private static final int SHIFTS_PER_PAGE = 3;

    private final SchedulingService schedulingService;

    public DashboardController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @GetMapping("/")
    public String dashboard(@RequestParam(defaultValue = "1") int page, Model model) {
        DashboardView dashboard = schedulingService.loadDashboard();
        int totalShifts = dashboard.shifts().size();
        int totalPages = Math.max(1, (int) Math.ceil(totalShifts / (double) SHIFTS_PER_PAGE));
        int currentPage = Math.min(Math.max(page, 1), totalPages);
        int fromIndex = Math.min((currentPage - 1) * SHIFTS_PER_PAGE, totalShifts);
        int toIndex = Math.min(fromIndex + SHIFTS_PER_PAGE, totalShifts);

        DashboardView pagedDashboard = new DashboardView(
                dashboard.summary(),
                dashboard.teamLoads(),
                dashboard.coverageRisks(),
                dashboard.shifts().subList(fromIndex, toIndex)
        );

        model.addAttribute("dashboard", pagedDashboard);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasPreviousPage", currentPage > 1);
        model.addAttribute("hasNextPage", currentPage < totalPages);
        return "dashboard";
    }

    @PostMapping("/shifts/{shiftId}/assign")
    public String assignEmployee(
            @PathVariable Long shiftId,
            @RequestParam @NotNull Long employeeId,
            @RequestParam(defaultValue = "1") int page,
            RedirectAttributes redirectAttributes
    ) {
        try {
            schedulingService.assignEmployee(shiftId, employeeId);
            redirectAttributes.addFlashAttribute("message", "Assignment created successfully.");
        } catch (AssignmentRuleViolationException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/?page=" + Math.max(page, 1);
    }
}
