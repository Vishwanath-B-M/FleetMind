
package com.rideshare.agent.controller;

import com.rideshare.agent.model.InvestigationRequest;
import com.rideshare.agent.service.AiRideInvestigationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent/investigations")
public class AiRideAgentController {

    private final AiRideInvestigationService aiInvestigationService;

    public AiRideAgentController(
            AiRideInvestigationService aiInvestigationService) {
        this.aiInvestigationService = aiInvestigationService;
    }

    @PostMapping("/ai")
    public String investigateWithAI(
            @RequestBody InvestigationRequest request) {

        return aiInvestigationService.investigate(
                request.rideId(),
                request.question()
        );
    }
}
