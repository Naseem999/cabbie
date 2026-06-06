package com.app.cabbie.service;

import com.app.cabbie.dto.KafkaEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;


    public void sendRideNotification(KafkaEventDTO dto){
        // Send a passenger-targeted notification event to the `ride-notifications` topic.
        // Typical usage: ride status updates (accepted, started, completed, canceled).
        kafkaTemplate.send("ride-notifications",dto);
        System.out.println("Published sendRideNotification→ " + dto);
    }

    public void sendRideRequestNotification(KafkaEventDTO dto){
        // Publish a new ride request event to the `ride-request` topic for driver assignment.
        // Triggers consumers that find and notify available drivers.
        System.out.println("Published sendRideRequestNotification→ " + dto);
        kafkaTemplate.send("ride-request", dto);
    }



}
