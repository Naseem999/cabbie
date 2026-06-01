package com.app.cabbie.service;

import com.app.cabbie.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;


    public void sendRideNotification(NotificationDTO dto){
//        NotificationDTO dto = new NotificationDTO(
//                userId,
//                "Ride Requested!",
//                "Your ride has been requested from " + pickupLocation + ". Finding driver..."
//        );
//        kafkaTemplate.send("ride-notifications", dto);
        kafkaTemplate.send("ride-notifications",dto);
        System.out.println("Published → " + dto);
    }

    public void sendRideRequestNotification(Long rideId, Long userId, String pickupLocation){
        NotificationDTO dto = new NotificationDTO(
                userId,
                "Ride Requested!",
                "Your ride has been requested from " + pickupLocation + ". Finding driver..."
        );
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("dto",dto);
        dataMap.put("rideId",rideId);

        System.out.println("Published → " + dto);

        kafkaTemplate.send("ride-request", dataMap);
    }

//
//    public void sendDriverAssignedNotification(Long userId, String driverName){
//        NotificationDTO dto = new NotificationDTO(
//                userId,
//                "Driver Assigned!",
//                "Your Driver " + driverName + "is on the way."
//        );
//        kafkaTemplate.send("driver-assigned", dto);
//        System.out.println("Published → " + dto);
//    }
//
//    public void sendRideCompletedNotification(Long userId, Double fare ){
//        NotificationDTO dto = new NotificationDTO(
//                userId,
//                "Ride Completed!",
//                "Your ride is done. Fare: Rs." +fare
//        );
//        kafkaTemplate.send("ride-completed", dto);
//        System.out.println("Published → " + dto);
//    }
//
//    public void sendRideCompletedNotification(Long userId, Double fare ){
//        NotificationDTO dto = new NotificationDTO(
//                userId,
//                "Ride Completed!",
//                "Your ride is done. Fare: Rs." +fare
//        );
//        kafkaTemplate.send("ride-completed", dto);
//        System.out.println("Published → " + dto);
//    }
//
//    public void sendCanceledNotification(Long userId, String pickupLocation){
//        NotificationDTO dto = new NotificationDTO(
//                userId,
//                "Ride Canceled!",
//                "Your ride for"+ pickupLocation+" has been canceled"
//        );
//        kafkaTemplate.send("rides", dto);
//        System.out.println("Published → " + dto);
//    }
//


}
