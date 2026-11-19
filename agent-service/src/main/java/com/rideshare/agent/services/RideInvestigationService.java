

package com.rideshare.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideshare.agent.model.InvestigationRequest;
import com.rideshare.agent.model.InvestigationResponse;
import com.rideshare.agent.tools.LocationInvestigationTool;
import com.rideshare.agent.tools.MatchingInvestigationTool;
import com.rideshare.agent.tools.RideInvestigationTool;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RideInvestigationService {

    private final RideInvestigationTool rideTool;
    private final MatchingInvestigationTool matchingTool;
    private final LocationInvestigationTool locationTool;
    private final ObjectMapper objectMapper;

    public RideInvestigationService(
            RideInvestigationTool rideTool,
            MatchingInvestigationTool matchingTool,
            LocationInvestigationTool locationTool,
            ObjectMapper objectMapper) {

        this.rideTool = rideTool;
        this.matchingTool = matchingTool;
        this.locationTool = locationTool;
        this.objectMapper = objectMapper;
    }

    public InvestigationResponse investigate(
            InvestigationRequest request) {

        List<String> evidence = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        // 1. Retrieve ride details
        String rideDetails = rideTool.getRideDetails(request.rideId());
        JsonNode ride = parseJson(rideDetails);

        evidence.add("Ride details: " + rideDetails);

        String rideStatus = ride.path("status").asText("UNKNOWN");

        // 2. Retrieve matching attempts
        String matchingAttempts;

        try {
            matchingAttempts =
                    matchingTool.getMatchingAttempts(request.rideId());

            evidence.add("Matching attempts: " + matchingAttempts);

        } catch (Exception exception) {
            matchingAttempts = null;
            evidence.add("Matching service unavailable: "
                    + exception.getMessage());
        }

        JsonNode matching = matchingAttempts != null
                ? parseJson(matchingAttempts)
                : objectMapper.createArrayNode();

        JsonNode latestAttempt =
                matching.isArray() && !matching.isEmpty()
                        ? matching.get(0)
                        : objectMapper.createObjectNode();

        String matchingStatus =
                latestAttempt.path("status").asText("UNKNOWN");

        String selectedDriver =
                latestAttempt.path("selectedDriverId").asText("NONE");

        String failureReason =
                latestAttempt.path("failureReason").asText("");

        // 3. Retrieve nearby drivers
        String nearbyDrivers;
        int nearbyDriverCount = -1;

        try {
            double latitude = ride.path("pickupLatitude").asDouble();
            double longitude = ride.path("pickupLongitude").asDouble();

            nearbyDrivers = locationTool.getNearbyDrivers(
                    latitude, longitude, 5.0
            );

            evidence.add("Nearby drivers: " + nearbyDrivers);

            JsonNode drivers = parseJson(nearbyDrivers);
            nearbyDriverCount = drivers.isArray()
                    ? drivers.size()
                    : -1;

        } catch (Exception exception) {
            evidence.add("Location service unavailable: "
                    + exception.getMessage());
        }

        // 4. Analyze collected evidence
        String severity;
        String summary;
        String likelyCause;

        if ("FAILED".equalsIgnoreCase(matchingStatus)) {

            severity = "HIGH";
            summary = "Driver matching failed for this ride.";

            likelyCause = !failureReason.isBlank()
                    ? failureReason
                    : "Matching service reported a failure.";

            recommendations.add(
                    "Investigate the matching failure reason."
            );
            recommendations.add(
                    "Verify driver availability and location updates."
            );

        } else if (nearbyDriverCount == 0
                && !"ACCEPTED".equalsIgnoreCase(rideStatus)) {

            severity = "HIGH";
            summary = "No nearby drivers were found.";

            likelyCause =
                    "Location service returned zero nearby drivers.";

            recommendations.add(
                    "Check driver location updates."
            );
            recommendations.add(
                    "Review the configured search radius."
            );

        } else if ("MATCHING".equalsIgnoreCase(rideStatus)) {

            severity = "MEDIUM";
            summary = "Ride is still waiting for a matching outcome.";

            likelyCause =
                    "The ride has not reached an accepted state.";

            recommendations.add(
                    "Inspect the latest matching attempt."
            );
            recommendations.add(
                    "Check Kafka consumer processing."
            );

        } else {

            severity = "LOW";
            summary = "Investigation evidence collected.";

            likelyCause =
                    "No matching failure is evident in the available data.";

            recommendations.add(
                    "Continue monitoring the ride lifecycle."
            );
        }

        evidence.add("Ride status: " + rideStatus);
        evidence.add("Matching status: " + matchingStatus);
        evidence.add("Nearby driver count: "
                + (nearbyDriverCount >= 0
                ? nearbyDriverCount
                : "UNAVAILABLE"));
        evidence.add("Selected driver: " + selectedDriver);

        return new InvestigationResponse(
                request.rideId(),
                severity,
                summary,
                likelyCause,
                evidence,
                recommendations
        );
    }

    private JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Invalid JSON received from a microservice",
                    exception
            );
        }
    }
}
