package com.atheboy.shiftpilot.web;

import com.atheboy.shiftpilot.service.DashboardView;
import com.atheboy.shiftpilot.service.SchedulingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final SchedulingService schedulingService;

    public DashboardApiController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @GetMapping
    public DashboardView dashboard() {
        return schedulingService.loadDashboard();
    }
}
