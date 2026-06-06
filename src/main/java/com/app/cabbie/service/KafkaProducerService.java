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
        kafkaTemplate.send("ride-notifications",dto);
        System.out.println("Published sendRideNotification→ " + dto);
    }

    public void sendRideRequestNotification(KafkaEventDTO dto){
        System.out.println("Published sendRideRequestNotification→ " + dto);
        kafkaTemplate.send("ride-request", dto);
    }



}
