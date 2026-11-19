
package com.rideshare.matchingservice.service;

import com.rideshare.matchingservice.client.LocationServiceClient;
import com.rideshare.matchingservice.dto.NearByDriverResponse;
import com.rideshare.matchingservice.entity.MatchingAttempt;
import com.rideshare.matchingservice.event.RideMatchedEvent;
import com.rideshare.matchingservice.event.RideRequestedEvent;
import com.rideshare.matchingservice.repository.MatchingAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchingService {

    private final LocationServiceClient locationServiceClient;
    private final KafkaTemplate<String, RideMatchedEvent> kafkaTemplate;
    private final MatchingAttemptRepository matchingAttemptRepository;

    private static final String RIDE_MATCHED_TOPIC = "ride.matched";
    private static final double DEFAULT_SEARCH_RADIUS_KM = 5.0;

    public void matchDriverForRide(RideRequestedEvent event) {

        MatchingAttempt attempt = new MatchingAttempt();
        attempt.setRideId(event.getRideId().toString());
        attempt.setStatus("IN_PROGRESS");
        attempt.setNearbyDriverCount(0);
        attempt.setSearchRadiusKm(DEFAULT_SEARCH_RADIUS_KM);
        attempt.setAttemptedAt(LocalDateTime.now());

        attempt = matchingAttemptRepository.save(attempt);

        try {
            List<NearByDriverResponse> nearByDrivers =
                    locationServiceClient.getNearByDrivers(
                            event.getPickupLatitude(),
                            event.getPickupLongitude(),
                            DEFAULT_SEARCH_RADIUS_KM
                    );

            attempt.setNearbyDriverCount(nearByDrivers.size());

            if (nearByDrivers.isEmpty()) {
                attempt.setStatus("FAILED");
                attempt.setFailureReason("NO_DRIVERS_AVAILABLE");
                matchingAttemptRepository.save(attempt);

                log.warn("No drivers found near ride {}", event.getRideId());
                return;
            }

            Optional<NearByDriverResponse> bestDriver =
                    findBestDriver(nearByDrivers);

            if (bestDriver.isEmpty()) {
                attempt.setStatus("FAILED");
                attempt.setFailureReason("NO_SUITABLE_DRIVER");
                matchingAttemptRepository.save(attempt);

                log.warn("Could not find suitable driver for ride {}",
                        event.getRideId());
                return;
            }

            NearByDriverResponse assignedDriver = bestDriver.get();

            attempt.setSelectedDriverId(
                    assignedDriver.getDriverId().toString()
            );

            RideMatchedEvent matchedEvent = new RideMatchedEvent(
                    event.getRideId().toString(),
                    event.getRiderId().toString(),
                    assignedDriver.getDriverId(),
                    assignedDriver.getLatitude(),
                    assignedDriver.getLongitude(),
                    assignedDriver.getDistanceInKm()
            );

            // Wait for Kafka broker acknowledgement.
            kafkaTemplate.send(
                    RIDE_MATCHED_TOPIC,
                    event.getRideId().toString(),
                    matchedEvent
            ).get(10, TimeUnit.SECONDS);

            attempt.setStatus("SUCCESS");
            matchingAttemptRepository.save(attempt);

            log.info("RideMatchedEvent published for ride {} with driver {}",
                    event.getRideId(), assignedDriver.getDriverId());

        } catch (Exception exception) {

            attempt.setStatus("FAILED");
            attempt.setFailureReason(
                    exception.getClass().getSimpleName()
            );
            matchingAttemptRepository.save(attempt);

            log.error("Matching failed for ride {}",
                    event.getRideId(), exception);

            throw new RuntimeException(
                    "Failed to match driver for ride "
                            + event.getRideId(),
                    exception
            );
        }
    }

    private Optional<NearByDriverResponse> findBestDriver(
            List<NearByDriverResponse> drivers) {

        double distanceWeight = 0.7;
        double ratingWeight = 0.3;

        return drivers.stream()
                .max(Comparator.comparingDouble(driver -> {

                    double distanceScore =
                            1.0 / (driver.getDistanceInKm() + 0.1);

                    double simulatedRating = 4.0 + Math.random();

                    return (distanceScore * distanceWeight)
                            + (simulatedRating * ratingWeight);
                }));
    }
}
