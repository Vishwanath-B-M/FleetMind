

package com.rideshare.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RideInvestigationTool {

    private final RestClient restClient;

    public RideInvestigationTool(
            @Value("${services.ride.url}") String rideServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(rideServiceUrl)
                .build();
    }

    @Tool(description = "Retrieve complete ride details using a ride ID. Use this when investigating a ride's status, rider, assigned driver, pickup, drop location, or fare.")
    public String getRideDetails(String rideId) {

        return restClient.get()
                .uri("/api/v1/rides/{rideId}", rideId)
                .retrieve()
                .body(String.class);
    }
}
