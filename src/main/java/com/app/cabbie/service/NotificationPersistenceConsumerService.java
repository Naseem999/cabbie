package com.app.cabbie.service;

import com.app.cabbie.dto.KafkaEventDTO;
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
    public void saveNotifications(KafkaEventDTO dto){
        NotificationDTO notificationDTO=NotificationDTO.builder()
                .userId(dto.getUserId())
                .title(dto.getTitle())
                .message(dto.getMessage())
                .build();
        notificationService.saveNotification(notificationDTO);
    }

    @KafkaListener(topics = "ride-request", groupId = "rideRequest-persistence")
    public void saveRideRequestNotifications(KafkaEventDTO dto){
        NotificationDTO notificationDTO=NotificationDTO.builder()
                .userId(dto.getUserId())
                .title(dto.getTitle())
                .message(dto.getMessage())
                .build();
        notificationService.saveNotification(notificationDTO);
    }

}
