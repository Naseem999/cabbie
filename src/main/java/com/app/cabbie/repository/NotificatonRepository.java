package com.app.cabbie.repository;

import com.app.cabbie.model.Notificaton;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificatonRepository extends JpaRepository<Notificaton, Long> {

    List<Notificaton> findByUserId(Long userId);
    
    List<Notificaton> findByReadStatus(Boolean readStatus);
}

