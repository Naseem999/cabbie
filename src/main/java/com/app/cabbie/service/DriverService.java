package com.app.cabbie.service;

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

@Service
public class DriverService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    DriverRepository driverRepository;

    @Transactional
    public Driver createNewDriver(Long userId){
        User savedUser= userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("User Not Found with User Id :"+ userId));
        Driver driver= Driver.builder()
                .user(savedUser)
                .build();
        return driverRepository.save(driver);
    }

    @Transactional
    public Driver updateVehicalDetails(Long driverId, VehicalDTO vehicalDTO){
        Driver savedDriver= driverRepository.findById(driverId).orElseThrow(()-> new UserNotFoundException("Driver Not Found With Id:"+ driverId));
        savedDriver.setVechicalModel(vehicalDTO.getVechicalModel());
        savedDriver.setVechicalNumber(vehicalDTO.getVechicalNumber());
        return driverRepository.save(savedDriver);
    }

    @Transactional
    public Driver updateDriverStatus(Long driverId, DriverStatus status){
        Driver savedDriver= driverRepository.findById(driverId).orElseThrow(()-> new UserNotFoundException("Driver Not Found With Id:"+ driverId));
        savedDriver.setDriverStatus(status);
        return driverRepository.save(savedDriver);
    }

}
