package com.atheboy.shiftpilot.web;

import com.atheboy.shiftpilot.service.CandidateRecommendation;
import com.atheboy.shiftpilot.service.SchedulingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class RecommendationApiController {

    private final SchedulingService schedulingService;

    public RecommendationApiController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @GetMapping("/{shiftId}/recommendations")
    public List<CandidateRecommendation> recommendations(@PathVariable Long shiftId) {
        return schedulingService.recommendationsForShift(shiftId);
    }
}
