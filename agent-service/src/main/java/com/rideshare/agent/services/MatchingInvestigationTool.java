
package com.rideshare.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MatchingInvestigationTool {

    private final RestClient restClient;

    public MatchingInvestigationTool(
            @Value("${services.matching.url}") String matchingServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(matchingServiceUrl)
                .build();
    }

    @Tool(description = "Retrieve all matching attempts for a ride, including status, nearby driver count, search radius, selected driver and failure reason.")
    public String getMatchingAttempts(String rideId) {

        return restClient.get()
                .uri("/api/v1/matching/attempts/{rideId}", rideId)
                .retrieve()
                .body(String.class);
    }
}
