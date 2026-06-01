package com.app.cabbie.service;

import com.app.cabbie.dto.NotificationDTO;
import com.app.cabbie.model.Notificaton;
import com.app.cabbie.model.User;
import com.app.cabbie.repository.NotificationRepository;
import com.app.cabbie.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Transactional
    public Notificaton saveNotification(NotificationDTO notificationDTO){
        try {
            User user= userRepository.findById(notificationDTO.getUserId()).orElseThrow(()-> new RuntimeException("User not found with UserId:"+notificationDTO.getUserId()));
            Notificaton notificaton=new Notificaton();
            notificaton.setUserId(user);
            notificaton.setTitle(notificationDTO.getTitle());
            notificaton.setMessage(notificationDTO.getMessage());
            notificaton.setReadStatus(false);
            return notificationRepository.save(notificaton);
        } catch (Exception e) {
            System.out.println("Exception While saving notification:"+e);
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public Notificaton updateNotificationReadStatus(Long  notificationId){
        try {
            notificationRepository.findById(notificationId).orElseThrow(()-> new RuntimeException("Notification not found with Id :"+notificationId));
            return notificationRepository.updateNotificationReadStatus(notificationId);
        } catch (Exception e) {
            System.out.println("Exception While updating notification:"+e);
            throw new RuntimeException(e);
        }
    }


    public List<Notificaton> getNotificationsByUserId(Long userId){
        try {
            return notificationRepository.findByUserId(userId);
        } catch (Exception e) {
            System.out.println("Exception While getting notifications:"+e);
            throw new RuntimeException(e);
        }
    }





}
