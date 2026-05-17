package com.app.cabbie.controller;

import com.app.cabbie.dto.RideRequestDTO;
import com.app.cabbie.enums.RideStatus;
import com.app.cabbie.model.Ride;
import com.app.cabbie.service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// REST API endpoints for managing ride requests, status updates, and ride history.
// Handles ride creation, acceptance, cancellation, and retrieval with role-based access control.
@RestController
@RequestMapping("/api/rides")
public class RidesController {


    @Autowired
    RideService rideService;

//    Passenger
//    =====================================================================

    // Allows a passenger to request a new ride with pickup and drop-off locations.
    // Returns the created ride object with calculated fare and REQUESTED status.
    @PostMapping("/request")
    @PreAuthorize("hasAnyRole('PASSENGER','ADMIN')")
    public ResponseEntity<Ride> requestRide(@RequestBody RideRequestDTO rideRequestDTO){
        Ride ride=rideService.requestRide(rideRequestDTO);
        return ResponseEntity.ok(ride);
    }


    // Cancels an existing ride by setting its status to CANCELED.
    // Can be initiated by passengers, drivers, or admins.
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PASSENGER','ADMIN','DRIVER')")
    public ResponseEntity<Ride> cancelRide(@PathVariable Long id){
        Ride ride=rideService.cancelRide(id);
        return ResponseEntity.ok(ride);
    }


    // Driver accepts a ride assignment and status changes from REQUESTED to ACCEPTED.
    // Driver's status also changes to BUSY indicating they are engaged in a ride.
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Ride> acceptAssigedRideRequest(@PathVariable Long id, Authentication authentication){

        Ride ride=rideService.acceptRideRequest(id, authentication.getName().trim());
        return ResponseEntity.ok(ride);
    }


    // Updates the ride status to IN_PROGRESS, COMPLETED, or other valid states.
    // Can be called by drivers and passengers to track ride progress.
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    public ResponseEntity<Ride> changeRideStatus(@PathVariable Long id, @RequestBody RideStatus status){
        Ride ride=rideService.updateRideStatus(id,status);
        return ResponseEntity.ok(ride);
    }


    // Admin only endpoint that retrieves all rides in the system.
    // Useful for monitoring and administrative purposes.
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Ride>> getAllRides(){
        List<Ride> rides=rideService.getAllRides();
        return ResponseEntity.ok(rides);
    }


    // Retrieves detailed information for a specific ride by ID.
    // Can be accessed by passengers, drivers, and admins.
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASSENGER','ADMIN','DRIVER')")
    public ResponseEntity<Ride> getRideDetails(@PathVariable Long id, Authentication authentication){
        Ride ride=rideService.getRideDetailsById(id,authentication);
        return ResponseEntity.ok(ride);
    }

    // Retrieves all past and current rides for a specific passenger.
    // User can only access their own ride history with role-based authorization.
    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId==principal.id && hasAnyRole('PASSENGER','ADMIN')")
    public ResponseEntity<List<Ride>> getRideDetailsByUserId(@PathVariable Long userId){
        List<Ride> rides=rideService.getRidesDetailsByUserId(userId);
        return ResponseEntity.ok(rides);
    }




}
