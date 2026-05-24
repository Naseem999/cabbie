package com.app.cabbie.service;

import com.app.cabbie.dto.LocationDTO;
import com.app.cabbie.dto.VehicleDTO;
import com.app.cabbie.enums.DriverStatus;
import com.app.cabbie.exceptions.UserNotFoundException;
import com.app.cabbie.model.Driver;
import com.app.cabbie.model.User;
import com.app.cabbie.repository.DriverRepository;
import com.app.cabbie.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages driver profiles, vehicle info and availability for the application.
 * Provides simple methods to create drivers, update vehicle info and location.
 */
@Service
public class DriverService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    DriverRepository driverRepository;

    // Converts an existing user to a driver by creating a new driver profile.
    // Links the user entity to the driver entity and saves it to the database.
    @Transactional
    public Driver createNewDriver(Long userId){
        User savedUser= userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("User Not Found with User Id :"+ userId));
        Driver driver= Driver.builder()
                .user(savedUser)
                .build();
        return driverRepository.save(driver);
    }

    // Updates the driver's vehicle model and registration number.
    // Allows drivers to modify their vehicle information.
    @Transactional
    public Driver updateVehicleDetails(Long driverId, VehicleDTO vehicleDTO){
        Driver savedDriver= driverRepository.findByUserId(driverId).orElseThrow(()-> new UserNotFoundException("Driver Not Found With Id:"+ driverId));
        savedDriver.setVechicalModel(vehicleDTO.getVechicalModel());
        savedDriver.setVechicalNumber(vehicleDTO.getVechicalNumber());
        return driverRepository.save(savedDriver);
    }

    // Updates the driver's status to OFFLINE, AVAILABLE, or BUSY.
    // Tracks driver's current availability and operational state.
    @Transactional
    public Driver updateDriverStatus(Long driverId, DriverStatus status){
        Driver savedDriver= driverRepository.findByUserId(driverId).orElseThrow(()-> new UserNotFoundException("Driver Not Found With Id:"+ driverId));
        savedDriver.setDriverStatus(status);
        return driverRepository.save(savedDriver);
    }

    // Updates driver's current location coordinates and sets status to AVAILABLE.
    // Used for real-time location tracking when drivers are ready for ride requests.
    @Transactional
    public Driver updateDriverLocation(Long driverId, LocationDTO locationDTO){
        Driver driver= driverRepository.findByUserId(driverId).orElseThrow(()-> new UserNotFoundException("Driver Not Found With Id:"+ driverId));
        driver.setCurrentLocationLat(locationDTO.getLatitude());
        driver.setCurrentLocationLng(locationDTO.getLongitude());
        driver.setDriverStatus(DriverStatus.AVAILABLE);
        return driverRepository.save(driver);
    }





}
