package com.app.cabbie.dto;

import com.app.cabbie.enums.RideStatus;
import com.app.cabbie.enums.RideType;
import lombok.Data;

@Data
public class RideRequestDTO {

    private Long userId;
    private String pickupLocation;
    private String dropLocation;
    private RideType rideType;
    private RideStatus rideStatus;
    private Double fare;
}
