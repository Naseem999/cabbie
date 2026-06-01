package com.app.cabbie.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class NotificationDTO {

    private Long userId;
    private String title;
    private String message;

}
