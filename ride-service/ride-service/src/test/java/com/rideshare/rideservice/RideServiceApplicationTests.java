
package com.rideshare.rideservice.service;

import com.rideshare.event.RideRequestedEvent;
import com.rideshare.rideservice.exception.InvalidRideStateException;
import com.rideshare.rideservice.exception.RideNotFoundException;
import com.rideshare.rideservice.model.Ride;
import com.rideshare.rideservice.model.RideStatus;
import com.rideshare.rideservice.repository.RideRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private KafkaTemplate<String, RideRequestedEvent> kafkaTemplate;

    @InjectMocks
    private RideService rideService;

    @Test
    void startRide_WhenRideIsAccepted_ShouldStartRide() {

        // Arrange
        Ride ride = new Ride();
        ride.setId(1L);
        ride.setRiderId("rider-101");
        ride.setStatus(RideStatus.ACCEPTED);

        when(rideRepository.findById(1L))
                .thenReturn(Optional.of(ride));

        // Act
        var response = rideService.startRide(1L);

        // Assert
        assertEquals(RideStatus.RIDE_STARTED, response.getStatus());
        assertNotNull(response.getStartedAt());

        verify(rideRepository).save(ride);
    }

    @Test
    void startRide_WhenRideIsNotAccepted_ShouldThrowException() {

        // Arrange
        Ride ride = new Ride();
        ride.setId(2L);
        ride.setRiderId("rider-102");
        ride.setStatus(RideStatus.REQUESTED);

        when(rideRepository.findById(2L))
                .thenReturn(Optional.of(ride));

        // Act and Assert
        assertThrows(
                InvalidRideStateException.class,
                () -> rideService.startRide(2L)
        );

        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void startRide_WhenRideDoesNotExist_ShouldThrowException() {

        // Arrange
        when(rideRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(
                RideNotFoundException.class,
                () -> rideService.startRide(999L)
        );

        verify(rideRepository, never()).save(any(Ride.class));
    }

@Test
void completeRide_WhenRideIsStarted_ShouldCompleteRide() {

    Ride ride = new Ride();
    ride.setId(3L);
    ride.setRiderId("rider-103");
    ride.setStatus(RideStatus.RIDE_STARTED);
    ride.setEstimatedFare(150.0);

    when(rideRepository.findById(3L))
            .thenReturn(Optional.of(ride));

    var response = rideService.completeRide(3L);

    assertEquals(RideStatus.COMPLETED, response.getStatus());
    assertNotNull(response.getCompletedAt());
    assertEquals(150.0, response.getActualFare());

    verify(rideRepository).save(ride);
}

@Test
void completeRide_WhenRideIsNotStarted_ShouldThrowException() {

    Ride ride = new Ride();
    ride.setId(4L);
    ride.setRiderId("rider-104");
    ride.setStatus(RideStatus.ACCEPTED);

    when(rideRepository.findById(4L))
            .thenReturn(Optional.of(ride));

    assertThrows(
            InvalidRideStateException.class,
            () -> rideService.completeRide(4L)
    );

    verify(rideRepository, never()).save(any(Ride.class));
}

@Test
void completeRide_WhenRideDoesNotExist_ShouldThrowException() {

    when(rideRepository.findById(999L))
            .thenReturn(Optional.empty());

    assertThrows(
            RideNotFoundException.class,
            () -> rideService.completeRide(999L)
    );

    verify(rideRepository, never()).save(any(Ride.class));
}

@Test
void cancelRide_WhenRideIsMatching_ShouldCancelRide() {

    Ride ride = new Ride();
    ride.setId(5L);
    ride.setRiderId("rider-105");
    ride.setStatus(RideStatus.MATCHING);

    when(rideRepository.findById(5L))
            .thenReturn(Optional.of(ride));

    var response = rideService.cancelRide(5L);

    assertEquals(RideStatus.CANCELLED, response.getStatus());

    verify(rideRepository).save(ride);
}

@Test
void cancelRide_WhenRideIsStarted_ShouldThrowException() {

    Ride ride = new Ride();
    ride.setId(6L);
    ride.setRiderId("rider-106");
    ride.setStatus(RideStatus.RIDE_STARTED);

    when(rideRepository.findById(6L))
            .thenReturn(Optional.of(ride));

    assertThrows(
            InvalidRideStateException.class,
            () -> rideService.cancelRide(6L)
    );

    verify(rideRepository, never()).save(any(Ride.class));
}

@Test
void cancelRide_WhenRideIsCompleted_ShouldThrowException() {

    Ride ride = new Ride();
    ride.setId(7L);
    ride.setRiderId("rider-107");
    ride.setStatus(RideStatus.COMPLETED);

    when(rideRepository.findById(7L))
            .thenReturn(Optional.of(ride));

    assertThrows(
            InvalidRideStateException.class,
            () -> rideService.cancelRide(7L)
    );

    verify(rideRepository, never()).save(any(Ride.class));
}

@Test
void cancelRide_WhenRideIsAlreadyCancelled_ShouldThrowException() {

    Ride ride = new Ride();
    ride.setId(8L);
    ride.setRiderId("rider-108");
    ride.setStatus(RideStatus.CANCELLED);

    when(rideRepository.findById(8L))
            .thenReturn(Optional.of(ride));

    assertThrows(
            InvalidRideStateException.class,
            () -> rideService.cancelRide(8L)
    );

    verify(rideRepository, never()).save(any(Ride.class));
}
@Test
void updateRideWithDriver_WhenRideIsMatching_ShouldAssignDriver() {

    Ride ride = new Ride();
    ride.setId(9L);
    ride.setRiderId("rider-109");
    ride.setStatus(RideStatus.MATCHING);

    when(rideRepository.findById(9L))
            .thenReturn(Optional.of(ride));

    rideService.updateRideWithDriver(9L, "driver-A");

    assertEquals("driver-A", ride.getDriverId());
    assertEquals(RideStatus.ACCEPTED, ride.getStatus());

    verify(rideRepository).save(ride);
}
@Test
void updateRideWithDriver_WhenRideIsAlreadyAccepted_ShouldRejectSecondDriver() {

    Ride ride = new Ride();
    ride.setId(10L);
    ride.setRiderId("rider-110");
    ride.setDriverId("driver-A");
    ride.setStatus(RideStatus.ACCEPTED);

    when(rideRepository.findById(10L))
            .thenReturn(Optional.of(ride));

    assertThrows(
            InvalidRideStateException.class,
            () -> rideService.updateRideWithDriver(10L, "driver-B")
    );

    assertEquals("driver-A", ride.getDriverId());
    assertEquals(RideStatus.ACCEPTED, ride.getStatus());

    verify(rideRepository, never()).save(any(Ride.class));
}

}
