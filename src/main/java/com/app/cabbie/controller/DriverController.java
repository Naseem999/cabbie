package com.app.cabbie.controller;

import com.app.cabbie.dto.VehicalDTO;
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
    @PreAuthorize("hasRole('DRIVER')")
    ResponseEntity<Driver> updateVehicalDetails(@PathVariable Long id, @RequestBody VehicalDTO vehicalDTO){
        Driver updatedDriverDetails=driverService.updateVehicalDetails(id,vehicalDTO);
        return new  ResponseEntity<>(updatedDriverDetails, HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('DRIVER')")
    ResponseEntity<Driver> updateVehicalDetails(@PathVariable Long id, @RequestBody DriverStatus status){
        Driver updatedDriverDetails=driverService.updateDriverStatus(id,status);
        return new  ResponseEntity<>(updatedDriverDetails, HttpStatus.OK);
    }


}
