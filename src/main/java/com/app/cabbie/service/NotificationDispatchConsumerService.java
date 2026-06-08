// Purpose: Kafka consumer service that dispatches ride notifications from Kafka topics to connected WebSocket/SSE clients.
// Notes: Listens on ride-notifications and ride-request topics; forwards events via SseEmitterRegistry for real-time delivery.

package com.app.cabbie.service;

import com.app.cabbie.dto.KafkaEventDTO;
import com.app.cabbie.dto.NotificationDTO;
import com.app.cabbie.model.Driver;
import com.app.cabbie.sse.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationDispatchConsumerService {

    private final RideService rideService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final SseEmitterRegistry registry;



    // Listen for ride events and forward them to the specific connected WebSocket user.
    // Sends the KafkaEventDTO payload to `/user/{email}/queue/notifications` so the client receives it.
    @KafkaListener(topics = "ride-notifications", groupId = "notification-dispatch")
    // Purpose: Consumes ride-notification events from Kafka and pushes them to connected SSE clients in real-time.
    // Behavior: Receives KafkaEventDTO, forwards to SseEmitterRegistry which sends event to matching user email.
    public void dispatchNotifications(KafkaEventDTO dto){
//        messagingTemplate.convertAndSendToUser(dto.getUserEmail(),"/queue/notifications",dto);
         registry.sendToUser("ride-notifications",dto);

    }


    // Handle incoming ride request events: assign a driver and notify both passenger and driver.
    // Assigns nearest driver (via RideService), pushes a driver-targeted Kafka event, and echoes passenger notification via WebSocket.
    @KafkaListener(topics = "ride-request", groupId = "rideRequest-dispatch")
    public void saveRideRequestNotifications(KafkaEventDTO dto){

        NotificationDTO notificationDTO=NotificationDTO.builder()
                .userId(dto.getUserId())
                .title(dto.getTitle())
                .message(dto.getMessage())
                .build();

            Long rideId=dto.getRideId();
            Driver driver= rideService.assignRideToNearestDriver(rideId);
//            rideService.acceptRideRequest(rideId,driver.getUser().getEmail());

            KafkaEventDTO newRideEvent= KafkaEventDTO.builder()
                    .userId(driver.getUser().getId())
                    .userEmail(driver.getUser().getEmail())
                    .title("New Ride!")
                    .message("You have a new Ride.")
                    .build();


//        messagingTemplate.convertAndSendToUser(dto.getUserEmail(),"/queue/notifications",dto);
        registry.sendToUser("ride-request",dto);

        kafkaTemplate.send("ride-notifications",newRideEvent);


    }


}
