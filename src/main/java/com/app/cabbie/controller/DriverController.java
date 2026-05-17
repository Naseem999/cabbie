package com.app.cabbie.controller;

import com.app.cabbie.dto.LocationDTO;
import com.app.cabbie.dto.VehicleDTO;
import com.app.cabbie.enums.DriverStatus;
import com.app.cabbie.model.Driver;
import com.app.cabbie.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    @Autowired
    DriverService driverService;

    @PutMapping("/{id}/vehicle")
    @PreAuthorize("#id==principal.id and hasRole('DRIVER') ")
    ResponseEntity<Driver> updateVehicleDetails(@PathVariable Long id, @RequestBody VehicleDTO vehicleDTO){
        Driver updatedDriverDetails=driverService.updateVehicleDetails(id, vehicleDTO);
        return new  ResponseEntity<>(updatedDriverDetails, HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("#id==principal.id and hasRole('DRIVER') ")
    ResponseEntity<Driver> updateDriverStatus(@PathVariable Long id, @RequestBody DriverStatus status){
        Driver updatedDriverDetails=driverService.updateDriverStatus(id,status);
        return new  ResponseEntity<>(updatedDriverDetails, HttpStatus.OK);
    }


    @PutMapping("/{driverId}/location")
    @PreAuthorize("#driverId==principal.id and hasRole('DRIVER')")
    public ResponseEntity<Driver> updateLocation(@PathVariable Long driverId,
                                            @RequestBody LocationDTO location) {
        return ResponseEntity.ok(driverService.updateDriverLocation(driverId,location));
    }


}
