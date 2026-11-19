
package com.rideshare.agent.model;

import java.util.List;

public record InvestigationResponse(
        String rideId,
        String severity,
        String summary,
        String likelyCause,
        List<String> evidence,
        List<String> recommendations
) {
}
