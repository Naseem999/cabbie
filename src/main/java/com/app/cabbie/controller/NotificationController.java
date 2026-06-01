package com.app.cabbie.controller;

import com.app.cabbie.dto.NotificationDTO;
import com.app.cabbie.model.Notificaton;
import com.app.cabbie.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    NotificationService notificationService;


    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId==principal.id  and  hasAnyRole('ADMIN','DRIVER','ADMIN')")
    public ResponseEntity<?> getAllNotificationsByUserId(@PathVariable Long userId){
        try {
            List<Notificaton> allNotifications =notificationService.getNotificationsByUserId(userId);
            return ResponseEntity.ok(allNotifications);
        }catch (Exception e){
            return ResponseEntity.internalServerError().body("Something went wrong");
        }

    }

    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> saveNotification(@RequestBody NotificationDTO notificationDTO){
        try {
            Notificaton notifications =notificationService.saveNotification(notificationDTO);
            return ResponseEntity.ok(notifications);
        }catch (Exception e){
            return ResponseEntity.internalServerError().body("Something went wrong");
        }

    }

    @PostMapping("/updateReadStatus/{notificationId}")
    @PreAuthorize("hasAnyRole('ADMIN','DRIVER','ADMIN')")
    public ResponseEntity<?> updateNotification(@PathVariable Long notificationId){
        try {
            Notificaton notifications =notificationService.updateNotificationReadStatus(notificationId);
            return ResponseEntity.ok(notifications);
        }catch (Exception e){
            return ResponseEntity.internalServerError().body("Something went wrong");
        }

    }




}
