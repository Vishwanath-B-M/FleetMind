
package com.rideshare.agent.service;

import com.rideshare.agent.tools.LocationInvestigationTool;
import com.rideshare.agent.tools.MatchingInvestigationTool;
import com.rideshare.agent.tools.RideInvestigationTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiRideInvestigationService {

    private final ChatClient chatClient;

    private final RideInvestigationTool rideTool;
    private final MatchingInvestigationTool matchingTool;
    private final LocationInvestigationTool locationTool;

    public AiRideInvestigationService(
            ChatClient.Builder chatClientBuilder,
            RideInvestigationTool rideTool,
            MatchingInvestigationTool matchingTool,
            LocationInvestigationTool locationTool) {

        this.chatClient = chatClientBuilder.build();
        this.rideTool = rideTool;
        this.matchingTool = matchingTool;
        this.locationTool = locationTool;
    }

    public String investigate(String rideId, String question) {

        return chatClient.prompt()
                .system("""
                    You are RideFlow AI, an intelligent ride-hailing
                    operations investigation agent.

                    Your responsibility is to investigate ride issues
                    using the available Java tools.

                    Instructions:

                    1. Retrieve ride details using the ride ID.
                    2. Use the pickup coordinates from ride details
                       when checking nearby drivers.
                    3. Retrieve matching attempts when investigating
                       driver assignment or matching problems.
                    4. Analyze the actual tool results.
                    5. Never invent data or claim a tool succeeded
                       if it failed.
                    6. Identify the likely cause based on evidence.
                    7. Recommend practical corrective actions.

                    Return a clear investigation report containing:
                    - Ride ID
                    - Ride status
                    - Severity: LOW, MEDIUM or HIGH
                    - Evidence collected
                    - Likely root cause
                    - Recommended actions
                    """)
                .user("""
                    Investigate ride ID: %s

                    User question: %s
                    """.formatted(rideId, question))
                .tools(rideTool, matchingTool, locationTool)
                .call()
                .content();
    }
}
