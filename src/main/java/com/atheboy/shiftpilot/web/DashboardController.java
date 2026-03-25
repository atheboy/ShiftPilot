package com.atheboy.shiftpilot.web;

import com.atheboy.shiftpilot.service.SchedulingService;
import com.atheboy.shiftpilot.service.AssignmentRuleViolationException;
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

    private final SchedulingService schedulingService;

    public DashboardController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("dashboard", schedulingService.loadDashboard());
        return "dashboard";
    }

    @PostMapping("/shifts/{shiftId}/assign")
    public String assignEmployee(
            @PathVariable Long shiftId,
            @RequestParam @NotNull Long employeeId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            schedulingService.assignEmployee(shiftId, employeeId);
            redirectAttributes.addFlashAttribute("message", "Assignment created successfully.");
        } catch (AssignmentRuleViolationException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/";
    }
}
