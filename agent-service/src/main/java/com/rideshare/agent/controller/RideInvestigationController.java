

package com.rideshare.agent.controller;

import com.rideshare.agent.tools.RideInvestigationTool;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent/rides")
public class RideInvestigationController {

    private final RideInvestigationTool rideInvestigationTool;

    public RideInvestigationController(
            RideInvestigationTool rideInvestigationTool) {
        this.rideInvestigationTool = rideInvestigationTool;
    }

    @GetMapping("/{rideId}")
    public String investigateRide(@PathVariable String rideId) {
        return rideInvestigationTool.getRideDetails(rideId);
    }
}
