
package com.rideshare.agent.controller;

import com.rideshare.agent.model.InvestigationRequest;
import com.rideshare.agent.model.InvestigationResponse;
import com.rideshare.agent.service.RideInvestigationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent/investigations")
public class RideAgentController {

    private final RideInvestigationService investigationService;

    public RideAgentController(
            RideInvestigationService investigationService) {
        this.investigationService = investigationService;
    }

    @PostMapping
    public InvestigationResponse investigate(
            @RequestBody InvestigationRequest request) {

        return investigationService.investigate(request);
    }
}
