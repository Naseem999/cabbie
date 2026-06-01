package com.app.cabbie.service;

import com.app.cabbie.dto.NotificationDTO;
import com.app.cabbie.model.Driver;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationDispatchConsumerService {

    private final RideService rideService;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @KafkaListener(topics = "ride-notifications", groupId = "notification-dispatch")
    public void dispatchNotifications(Object payload){
        NotificationDTO notificationDTO=(NotificationDTO) payload;
            System.out.println(notificationDTO.toString());

    }


    @KafkaListener(topics = "ride-request", groupId = "rideRequest-dispatch")
    public void saveRideRequestNotifications(ConsumerRecord<String, Object> record){
        Object payload=record.value();
        HashMap map=(HashMap)payload;
//        NotificationDTO notificationDTO=(NotificationDTO)map.get("dto");

        LinkedHashMap notificationLHM=(LinkedHashMap)map.get("dto");

        NotificationDTO notificationDTO=NotificationDTO.builder()
                .userId(Long.valueOf(notificationLHM.get("userId").toString()))
                .title(String.valueOf(notificationLHM.get("title")))
                .message(String.valueOf(notificationLHM.get("message")))
                .build();


            Long rideId=Long.valueOf(map.get("rideId").toString());
            Driver driver= rideService.assignRideToNearestDriver(rideId);
            rideService.acceptRideRequest(rideId,driver.getUser().getEmail());
            NotificationDTO notificationDTO1 = NotificationDTO.builder()
                    .userId(driver.getUser().getId())
                    .title("New Ride!")
                    .message("You have a new Ride.")
                    .build();

           kafkaTemplate.send("ride-notifications",notificationDTO1);


    }


}
