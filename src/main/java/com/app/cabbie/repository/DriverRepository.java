package com.app.cabbie.repository;

import com.app.cabbie.enums.DriverStatus;
import com.app.cabbie.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver,Long> {

    Optional<Driver> findByUserId(Long id);
    List<Driver> findByDriverStatus(DriverStatus driverStatus);
}
