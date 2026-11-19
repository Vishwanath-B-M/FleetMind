
package com.rideshare.rideservice.integration;

import com.rideshare.rideservice.model.Ride;
import com.rideshare.rideservice.model.RideStatus;
import com.rideshare.rideservice.repository.RideRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.junit.jupiter.api.Assertions;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.*;

@SpringBootTest
class RideOptimisticLockingIntegrationTest {

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

@Test
void shouldAllowOnlyOneDriverAssignmentWhenTwoTransactionsRunConcurrently()
        throws Exception {

    // Create a dedicated test ride
    Ride ride = new Ride();
    ride.setRiderId("optimistic-lock-test");
    ride.setPickupLatitude(12.9716);
    ride.setPickupLongitude(77.5946);
    ride.setPickupAddress("MG Road");
    ride.setDropLatitude(12.9352);
    ride.setDropLongitude(77.6245);
    ride.setDropAddress("Koramangala");
    ride.setStatus(RideStatus.MATCHING);

    Ride savedRide = rideRepository.saveAndFlush(ride);
    Long rideId = savedRide.getId();

    ExecutorService executor = Executors.newFixedThreadPool(2);
    CyclicBarrier bothLoaded = new CyclicBarrier(2);

    TransactionTemplate transactionTemplate =
            new TransactionTemplate(transactionManager);

    try {
        Callable<Throwable> assignDriver = () -> {
            try {
                transactionTemplate.executeWithoutResult(status -> {

                    Ride currentRide = rideRepository.findById(rideId)
                            .orElseThrow();

                    try {
                        // Both transactions must load the same version first
                        bothLoaded.await(10, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    currentRide.setDriverId(
                            Thread.currentThread().getName()
                    );
                    currentRide.setStatus(RideStatus.ACCEPTED);

                    rideRepository.saveAndFlush(currentRide);
                });

                return null;

            } catch (Throwable exception) {
                return exception;
            }
        };

        Future<Throwable> first =
                executor.submit(assignDriver);

        Future<Throwable> second =
                executor.submit(assignDriver);

        Throwable firstResult = first.get(20, TimeUnit.SECONDS);
        Throwable secondResult = second.get(20, TimeUnit.SECONDS);

        // Exactly one transaction should succeed
        Assertions.assertTrue(
                (firstResult == null) != (secondResult == null),
                "Exactly one transaction should succeed"
        );

        // The losing transaction must fail due to optimistic locking
        Throwable failure = firstResult != null
                ? firstResult : secondResult;

        boolean optimisticLockFailure = false;

        while (failure != null) {
            if (failure instanceof OptimisticLockingFailureException
                    || failure instanceof jakarta.persistence.OptimisticLockException) {
                optimisticLockFailure = true;
                break;
            }
            failure = failure.getCause();
        }

        Assertions.assertTrue(
                optimisticLockFailure,
                "The losing transaction should fail because of optimistic locking"
        );

    } finally {
        executor.shutdownNow();
        rideRepository.deleteById(rideId);
    }
}

}
