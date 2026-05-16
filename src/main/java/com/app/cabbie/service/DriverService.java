package com.app.cabbie.service;

import com.app.cabbie.dto.LocationDTO;
import com.app.cabbie.dto.VehicalDTO;
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
 * DriverService handles all driver-related business logic and operations.
 * This service manages driver registration, vehicle details, status updates,
 * and location tracking for drivers in the Cabbie application.
 */
@Service
public class DriverService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    DriverRepository driverRepository;

    /**
     * Creates a new driver record for an existing user.
     * This method initializes a driver profile by linking a user to the driver entity.
     * 
     * @param userId The ID of the user to be converted to a driver
     * @return The newly created Driver object with associated user information
     * @throws UserNotFoundException if the user with given userId is not found
     */
    @Transactional
    public Driver createNewDriver(Long userId){
        User savedUser= userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("User Not Found with User Id :"+ userId));
        Driver driver= Driver.builder()
                .user(savedUser)
                .build();
        return driverRepository.save(driver);
    }

    /**
     * Updates vehicle details (model and number) for a driver.
     * This method allows drivers to update their vehicle information including
     * the vehicle model and registration number.
     * 
     * @param driverId The ID of the driver whose vehicle details need to be updated
     * @param vehicalDTO The data transfer object containing vehicle model and number
     * @return The updated Driver object with new vehicle details
     * @throws UserNotFoundException if the driver with given driverId is not found
     */
    @Transactional
    public Driver updateVehicalDetails(Long driverId, VehicalDTO vehicalDTO){
        Driver savedDriver= driverRepository.findById(driverId).orElseThrow(()-> new UserNotFoundException("Driver Not Found With Id:"+ driverId));
        savedDriver.setVechicalModel(vehicalDTO.getVechicalModel());
        savedDriver.setVechicalNumber(vehicalDTO.getVechicalNumber());
        return driverRepository.save(savedDriver);
    }

    /**
     * Updates the driver's current status.
     * This method changes the driver's operational status which can be OFFLINE, AVAILABLE,
     * or ON_RIDE to track their availability and current state.
     * 
     * @param driverId The ID of the driver whose status needs to be updated
     * @param status The new DriverStatus value to set
     * @return The updated Driver object with new status
     * @throws UserNotFoundException if the driver with given driverId is not found
     */
    @Transactional
    public Driver updateDriverStatus(Long driverId, DriverStatus status){
        Driver savedDriver= driverRepository.findById(driverId).orElseThrow(()-> new UserNotFoundException("Driver Not Found With Id:"+ driverId));
        savedDriver.setDriverStatus(status);
        return driverRepository.save(savedDriver);
    }

    /**
     * Updates the driver's current location coordinates and marks them as AVAILABLE.
     * This method is used for real-time location tracking of drivers. When a driver's
     * location is updated, their status is automatically set to AVAILABLE to indicate
     * they are ready to accept ride requests.
     * 
     * @param driverId The ID of the driver whose location needs to be updated
     * @param locationDTO The data transfer object containing latitude and longitude
     * @return The updated Driver object with new location and AVAILABLE status
     * @throws UserNotFoundException if the driver with given driverId is not found
     */
    @Transactional
    public Driver updateDriverLocation(Long driverId, LocationDTO locationDTO){
        Driver driver= driverRepository.findById(driverId).orElseThrow(()-> new UserNotFoundException("Driver Not Found With Id:"+ driverId));
        driver.setCurrentLocationLat(locationDTO.getLatitude());
        driver.setCurrentLocationLng(locationDTO.getLongitude());
        driver.setDriverStatus(DriverStatus.AVAILABLE);
        return driverRepository.save(driver);
    }


}
