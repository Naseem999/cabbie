package com.app.cabbie.service;

import com.app.cabbie.dto.RideRequestDTO;
import com.app.cabbie.model.Ride;
import com.app.cabbie.repository.RidesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PassengerService {

    @Autowired
    RidesRepository rideRepository;

//    public Ride BookRide(RideRequestDTO rideRequestDTO){
//
//    }

}
