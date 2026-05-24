package com.app.cabbie.service;

import com.app.cabbie.dto.LocationDTO;
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

    @Value("${google.maps.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Create a new ride request from passenger
    @Transactional
    public Ride requestRide(RideRequestDTO rideRequestDTO){
        User passenger = userRepository.findById(rideRequestDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not Found with Id:" + rideRequestDTO.getUserId()));

        Ride newRide = Ride.builder()
                .passengerId(passenger)
                .pickupLocation(rideRequestDTO.getPickupLocation())
                .dropLocation(rideRequestDTO.getDropLocation())
                .rideType(rideRequestDTO.getRideType())
                .fare(dynamicFareCalculationService.calculateFare(
                        rideRequestDTO.getPickupLocationDTO().getLatitude(),
                        rideRequestDTO.getPickupLocationDTO().getLongitude(),
                        rideRequestDTO.getDropLocationDTO().getLatitude(),
                        rideRequestDTO.getDropLocationDTO().getLongitude(),
                        rideRequestDTO.getRideType()
                ))
                .rideStatus(RideStatus.REQUESTED)
                .build();

        return ridesRepository.save(newRide);
    }

    // Find and assign ride to the nearest available driver
    @Transactional
    public Driver assignRideToNearestDriver(LocationDTO locationDTO){
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
        return nearest;
    }

    // Driver accepts a ride request and update driver status
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
        return ridesRepository.save(ride);
    }

    // Update ride status
    @Transactional
    public Ride updateRideStatus(Long rideId, RideStatus status){
        Ride ride = ridesRepository.findById(rideId).orElseThrow(() -> new RuntimeException("Ride Not Found"));
        ride.setRideStatus(status);
        return ridesRepository.save(ride);
    }

    // Cancel a ride
    @Transactional
    public Ride cancelRide(Long id){
        Ride ride = ridesRepository.findById(id).orElseThrow(() -> new RuntimeException("Ride not found."));
        ride.setRideStatus(RideStatus.CANCELED);
        return ridesRepository.save(ride);
    }

    // Get ride details by id with simple authorization check
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

    // Get rides for a specific passenger
    public List<Ride> getRidesDetailsByUserId(Long userId){
        return ridesRepository.findByPassengerId(userId).orElseThrow(() -> new RuntimeException("NO Ride Found for UserId:" + userId));
    }

    // Get all rides
    public List<Ride> getAllRides(){
        return ridesRepository.findAll();
    }


}
