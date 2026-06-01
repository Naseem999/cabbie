package com.app.cabbie.repository;

import com.app.cabbie.model.Notificaton;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notificaton, Long> {

    List<Notificaton> findByUserId(Long userId);

    List<Notificaton> findByReadStatus(Boolean readStatus);

    @Modifying
    @Query(value = "update notifications set read_status=true where id=:notificationId;", nativeQuery = true)
    Notificaton updateNotificationReadStatus(@Param("notificationId") Long notificationId);
}

