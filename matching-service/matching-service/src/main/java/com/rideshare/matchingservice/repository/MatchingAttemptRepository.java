
package com.rideshare.matchingservice.repository;

import com.rideshare.matchingservice.entity.MatchingAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchingAttemptRepository
        extends JpaRepository<MatchingAttempt, Long> {

    List<MatchingAttempt> findByRideIdOrderByAttemptedAtDesc(String rideId);

    Optional<MatchingAttempt> findFirstByRideIdOrderByAttemptedAtDesc(String rideId);
}
