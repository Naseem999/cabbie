package com.app.cabbie.service;

import com.app.cabbie.dto.LocationDTO;
import com.app.cabbie.dto.NotificationDTO;
import com.app.cabbie.dto.RideRequestDTO;
import com.app.cabbie.enums.DriverStatus;
import com.app.cabbie.enums.RideStatus;
import com.app.cabbie.exceptions.UserNotFoundException;
import com.app.cabbie.model.Driver;
import com.app.cabbie.model.Ride;
import com.app.cabbie.model.User;
import com.app.cabbie.repository.DriverRepository;
import com.app.cabbie.repository.RidesRepository;
import com.app.cabbie.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RideService manages ride requests, assignment and status updates.
 * Integrates fare calculation and driver selection using Google Maps APIs.
 */
@Service
public class RideService {

    @Autowired
    DriverRepository driverRepository;

    @Autowired
    RidesRepository ridesRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DynamicFareCalculationService dynamicFareCalculationService;

    @Autowired
    KafkaProducerService producerService;


    @Value("${google.maps.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a new ride request and publishes notification via Kafka event stream.
     * Calculates dynamic fare based on distance and notifies available drivers.
     */
    @Transactional
    public Ride requestRide(RideRequestDTO rideRequestDTO){
        User passenger = userRepository.findById(rideRequestDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not Found with Id:" + rideRequestDTO.getUserId()));
        try {
            double fare = dynamicFareCalculationService.calculateFare(
                    rideRequestDTO.getPickupLocationDTO().getLatitude(),
                    rideRequestDTO.getPickupLocationDTO().getLongitude(),
                    rideRequestDTO.getDropLocationDTO().getLatitude(),
                    rideRequestDTO.getDropLocationDTO().getLongitude(),
                    rideRequestDTO.getRideType()
            );

            Ride newRide = Ride.builder()
                    .passengerId(passenger)
                    .pickupLocation(rideRequestDTO.getPickupLocation())
                    .dropLocation(rideRequestDTO.getDropLocation())
                    .pickupLocationLatitude(rideRequestDTO.getPickupLocationDTO().getLatitude())
                    .pickupLocationLongitude(rideRequestDTO.getPickupLocationDTO().getLongitude())
                    .dropLocationLatitude(rideRequestDTO.getDropLocationDTO().getLatitude())
                    .dropLocationLongitude(rideRequestDTO.getDropLocationDTO().getLongitude())
                    .rideType(rideRequestDTO.getRideType())
                    .fare(fare)
                    .rideStatus(RideStatus.REQUESTED)
                    .build();
            Ride savedRide= ridesRepository.save(newRide);

            producerService.sendRideRequestNotification(savedRide.getId(),savedRide.getPassengerId().getId(), savedRide.getPickupLocation());

            return savedRide;
        } catch (Exception e) {
            // Wrap and rethrow so transaction can be rolled back and caller gets meaningful message
            throw new RuntimeException("Failed to create ride: " + e.getMessage(), e);
        }
    }

    /**
     * Finds the nearest available driver using Google Maps Distance Matrix API.
     * Selects driver with minimum travel duration and assigns them to the ride.
     */
    @Transactional
    public Driver assignRideToNearestDriver(Long rideId){

        Ride ride=ridesRepository.findById(rideId).orElseThrow(()->new RuntimeException("Ride not found."));
        LocationDTO locationDTO= new LocationDTO();
        locationDTO.setLatitude(ride.getPickupLocationLatitude());
        locationDTO.setLongitude(ride.getPickupLocationLongitude());
        List<Driver> drivers = driverRepository.findByDriverStatus(DriverStatus.AVAILABLE);
        if (drivers.isEmpty()) throw new RuntimeException("No drivers available");

        String destinations = drivers.stream()
                .map(d -> d.getCurrentLocationLat() + "," + d.getCurrentLocationLng())
                .collect(Collectors.joining("|"));

        String origin = locationDTO.getLatitude() + "," + locationDTO.getLongitude();

        String url = String.format("https://maps.googleapis.com/maps/api/distancematrix/json"
                + "?origins=" + origin
                + "&destinations=" + destinations
                + "&mode=driving"
                + "&key=" + apiKey);

        String response = restTemplate.getForObject(url, String.class);
        JsonNode root;
        try {
            root = objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        JsonNode elements = root.path("rows").get(0).path("elements");

        long minDuration = Long.MAX_VALUE;
        Driver nearest = null;

        for (int i = 0; i < drivers.size(); i++) {
            JsonNode el = elements.get(i);
            if ("OK".equals(el.path("status").asText())) {
                long duration = el.path("duration").path("value").asLong(); // seconds
                if (duration < minDuration) {
                    minDuration = duration;
                    nearest = drivers.get(i);
                }
            }
        }

        ride.setDriverId(nearest);
        ridesRepository.save(ride);

        return nearest;
    }

    /**
     * Accepts a ride request by driver and sends notification via Kafka to passenger.
     * Verifies driver ownership and updates driver status to BUSY upon acceptance.
     */
    @Transactional
    public Ride acceptRideRequest(Long rideId, String driverEmail){
        Ride ride = ridesRepository.findById(rideId).orElseThrow(() -> new RuntimeException("Ride Not Found"));
        Driver driver = ride.getDriverId();

        if (driver == null || !driver.getUser().getEmail().equals(driverEmail)){
            throw new RuntimeException("Access Denied");
        }

        ride.setRideStatus(RideStatus.ACCEPTED);
        driver.setDriverStatus(DriverStatus.BUSY);


        driverRepository.save(driver);

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .title("Ride Accepted!")
                .message("Driver Accepted The Ride.")
                .userId(ride.getPassengerId().getId())
                .build();

        producerService.sendRideNotification(notificationDTO);

        return ridesRepository.save(ride);
    }

    /**
     * Updates ride status and publishes status-specific Kafka events to passenger.
     * Sends distinct notifications for IN_PROGRESS, COMPLETED, and other state transitions.
     */
    @Transactional
    public Ride updateRideStatus(Long rideId, RideStatus status){
        Ride ride = ridesRepository.findById(rideId).orElseThrow(() -> new RuntimeException("Ride Not Found"));
        ride.setRideStatus(status);
        Ride savedRide=ridesRepository.save(ride);

        // Handle different ride status transitions with appropriate notifications
        switch (savedRide.getRideStatus()) {
            case IN_PROGRESS:
                NotificationDTO startedNotification = NotificationDTO.builder()
                        .title("Ride Started!")
                        .message("Your ride has started.")
                        .userId(ride.getPassengerId().getId())
                        .build();
                producerService.sendRideNotification(startedNotification);
                break;

            case COMPLETED:
                NotificationDTO completedNotification = NotificationDTO.builder()
                        .title("Ride Completed!")
                        .message("Your ride has been completed. Please rate your driver.")
                        .userId(ride.getPassengerId().getId())
                        .build();
                producerService.sendRideNotification(completedNotification);
                break;
        }
        return savedRide;
    }

    /**
     * Cancels a ride and publishes cancellation notification via Kafka to passenger.
     * Records cancellation status for audit trail and refund processing.
     */
    @Transactional
    public Ride cancelRide(Long id){
        Ride ride = ridesRepository.findById(id).orElseThrow(() -> new RuntimeException("Ride not found."));
        ride.setRideStatus(RideStatus.CANCELED);
        Ride savedRide= ridesRepository.save(ride);

        if(savedRide.getRideStatus().equals(RideStatus.CANCELED)){
            NotificationDTO canceledRideNotification = NotificationDTO.builder()
                    .title("Ride Canceled!")
                    .message("Your ride has been canceled.")
                    .userId(ride.getPassengerId().getId())
                    .build();
            producerService.sendRideNotification(canceledRideNotification);
        }

        return savedRide;
    }

    /**
     * Retrieves ride details with authentication-based authorization checks.
     * Only driver, passenger, or admin can access ride information via Spring Security context.
     */
    public Ride getRideDetailsById(Long id, Authentication authentication){
        Ride ride = ridesRepository.findById(id).orElseThrow(() -> new RuntimeException("Ride not found."));
        boolean isDriver = ride.getDriverId() != null && ride.getDriverId().getUser().getEmail().equals(authentication.getName());
        boolean isPassenger = ride.getPassengerId() != null && ride.getPassengerId().getEmail().equals(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isDriver || isPassenger || isAdmin) {
            return ride;
        } else {
            throw new RuntimeException("Access Denied.");
        }
    }

    /**
     * Fetches all rides for a specific passenger/user by their ID.
     * Returns complete ride history for passenger dashboard and review purposes.
     */
    public List<Ride> getRidesDetailsByUserId(Long userId){
        return ridesRepository.findByPassengerId(userId).orElseThrow(() -> new RuntimeException("NO Ride Found for UserId:" + userId));
    }

    /**
     * Fetches all rides assigned to a specific driver by their ID.
     * Provides driver's complete ride history for earnings and performance tracking.
     */
    public List<Ride> getRidesDetailsByDriverId(Long driverId){

        Driver driver=driverRepository.findById(driverId).orElseThrow(()-> new RuntimeException("Driver Not found"));

        return ridesRepository.findByDriverId(driver);
    }

    /**
     * Retrieves all rides in the system (admin-only access).
     * Used for analytics, reporting, and platform-wide ride management.
     */
    public List<Ride> getAllRides(){
        return ridesRepository.findAll();
    }


}
