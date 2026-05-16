package com.app.cabbie.controller;

import com.app.cabbie.enums.RideType;
import com.app.cabbie.service.DynamicFareCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/rides")
public class FareEstimationController {

    @Autowired
    DynamicFareCalculationService fareCalculationService;
    @GetMapping("/estimate")
    @PreAuthorize("hasAnyRole('ADMIN','PASSENGER')")
    ResponseEntity<Map<String, Object>> estimate(@RequestParam double pickupLat,
                                                 @RequestParam double pickupLng,
                                                 @RequestParam double dropLat,
                                                 @RequestParam double dropLng,
                                                 @RequestParam String rideType){
        Map<String, Object> fareBreakup= fareCalculationService.calculateFare(pickupLat,pickupLng,dropLat,dropLng,RideType.valueOf(rideType.toUpperCase()));
        return new ResponseEntity<>(fareBreakup, HttpStatus.OK);

    }

}
