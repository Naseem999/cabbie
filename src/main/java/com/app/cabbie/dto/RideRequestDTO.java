package com.app.cabbie.dto;

import com.app.cabbie.enums.RideStatus;
import com.app.cabbie.enums.RideType;
import lombok.Data;

@Data
public class RideRequestDTO {
    private Long userId;
    private String pickupLocation;
    private String dropLocation;
    private LocationDTO pickupLocationDTO;
    private LocationDTO dropLocationDTO;
    private RideType rideType;
}
