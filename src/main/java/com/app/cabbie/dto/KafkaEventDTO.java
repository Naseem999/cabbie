package com.app.cabbie.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KafkaEventDTO {

    private String userEmail;
    private Long rideId;
    private Long userId;

    private String title;
    private String message;
}
