
package com.rideshare.matchingservice.controller;

import com.rideshare.matchingservice.entity.MatchingAttempt;
import com.rideshare.matchingservice.repository.MatchingAttemptRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/matching/attempts")
public class MatchingAttemptController {

    private final MatchingAttemptRepository matchingAttemptRepository;

    public MatchingAttemptController(
            MatchingAttemptRepository matchingAttemptRepository) {
        this.matchingAttemptRepository = matchingAttemptRepository;
    }

    @GetMapping("/{rideId}")
    public List<MatchingAttempt> getMatchingAttempts(
            @PathVariable String rideId) {

        return matchingAttemptRepository
                .findByRideIdOrderByAttemptedAtDesc(rideId);
    }

    @GetMapping("/{rideId}/latest")
    public MatchingAttempt getLatestMatchingAttempt(
            @PathVariable String rideId) {

        return matchingAttemptRepository
                .findFirstByRideIdOrderByAttemptedAtDesc(rideId)
                .orElseThrow(() -> new RuntimeException(
                        "No matching attempts found for ride: " + rideId
                ));
    }
}
