package com.app.cabbie.service;

import com.app.cabbie.enums.RideType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
public class DynamicFareCalculationService {

    @Value("${google.maps.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();


    public Map<String, Object> calculateFare(double pickupLat, double pickupLng,double dropLat,double dropLng, RideType rideType){
        String url = String.format(
                "https://maps.googleapis.com/maps/api/distancematrix/json" +
                        "?origins=%f,%f&destinations=%f,%f&units=metric&key=%s",
                pickupLat, pickupLng, dropLat, dropLng, apiKey
        );

        try {
            String raw = restTemplate.getForObject(url, String.class);
            JsonNode element = objectMapper.readTree(raw)
                    .path("rows").get(0)
                    .path("elements").get(0);

            double distanceKm      = element.path("distance").path("value").asDouble() / 1000.0;
            double durationMinutes = element.path("duration").path("value").asDouble() / 60.0;

            // Step 2: Calculate fare
            double fare = rideType.baseFare
                    + (rideType.perKmRate * distanceKm)
                    + (rideType.perMinuteRate * durationMinutes);

            fare = Math.max(fare, rideType.minimumFare);

            // Step 3: Return response
            return Map.of(
                    "rideType",     rideType,
                    "distanceKm",      Math.round(distanceKm * 100.0) / 100.0,
                    "durationMinutes", Math.round(durationMinutes * 100.0) / 100.0,
                    "totalFare",       Math.round(fare * 100.0) / 100.0
            );

        } catch (Exception e) {
            return Map.of("error", "Failed to calculate fare: " + e.getMessage());
        }
    }

}
