
package com.rideshare.matchingservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "matching_attempts")
@Getter
@Setter
public class MatchingAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String rideId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Integer nearbyDriverCount;

    @Column(nullable = false)
    private Double searchRadiusKm;

    private String selectedDriverId;

    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime attemptedAt;

    @PrePersist
    protected void onCreate() {
        if (attemptedAt == null) {
            attemptedAt = LocalDateTime.now();
        }
    }
}
