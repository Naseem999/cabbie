package com.app.cabbie.service;

import com.app.cabbie.dto.NotificationDTO;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationPersistenceConsumerService {

    private final NotificationService notificationService;

    @KafkaListener(topics = "ride-notifications", groupId = "notification-persistence")
    public void saveNotifications(Object payload){
        if(payload instanceof  NotificationDTO notificationDTO) {
            notificationService.saveNotification(notificationDTO);
        }
    }

    @KafkaListener(topics = "ride-request", groupId = "rideRequest-persistence")
    public void saveRideRequestNotifications(ConsumerRecord<String, Object> record){
       Object payload=record.value();
        HashMap dataMap = (HashMap) payload;
        LinkedHashMap notificationLHM=(LinkedHashMap)dataMap.get("dto");

        NotificationDTO notificationDTO=NotificationDTO.builder()
                        .userId(Long.valueOf(notificationLHM.get("userId").toString()))
                .title(String.valueOf(notificationLHM.get("title")))
                .message(String.valueOf(notificationLHM.get("message")))
                .build();

            notificationService.saveNotification(notificationDTO);
    }

}
