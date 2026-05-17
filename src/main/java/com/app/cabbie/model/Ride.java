package com.app.cabbie.model;

import com.app.cabbie.dto.LocationDTO;
import com.app.cabbie.enums.RideStatus;
import com.app.cabbie.enums.RideType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rides")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id" , referencedColumnName = "id")
    private User passengerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id" , referencedColumnName = "id", nullable = true)
    private Driver driverId;

    @Column(name = "pickup_location" ,columnDefinition = "VARCHAR(255)")
    private String pickupLocation;

    @Column(name = "drop_location" ,columnDefinition = "VARCHAR(255)")
    private String dropLocation;

    @Column(name = "pickup_location_latitude")
    private Double pickupLocationLatitude;

    @Column(name = "pickup_location_longitude" )
    private Double pickupLocationLongitude;

    @Column(name = "drop_location_latitude" )
    private Double dropLocationLatitude;

    @Column(name = "drop_location_longitude" )
    private Double dropLocationLongitude;


    @Enumerated(EnumType.STRING)
    @Column(name = "ride_type", length = 255)
    private RideType rideType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 255)
    private RideStatus rideStatus;

    @Column(name = "fare", columnDefinition = "DECIMAL(10,2)")
    private Double fare;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;



}
