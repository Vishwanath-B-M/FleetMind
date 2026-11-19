
package com.rideshare.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LocationInvestigationTool {

    private final RestClient restClient;

    public LocationInvestigationTool(
            @Value("${services.location.url}") String locationServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(locationServiceUrl)
                .build();
    }

    @Tool(description = "Find nearby drivers around a ride's pickup coordinates. Returns driver IDs, locations and distances.")
    public String getNearbyDrivers(
            double latitude,
            double longitude,
            double radiusKm) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/locations/driver/nearby")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("radius", radiusKm)
                        .build())
                .retrieve()
                .body(String.class);
    }
}
