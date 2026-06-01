package com.app.cabbie.repository;

import com.app.cabbie.enums.RideStatus;
import com.app.cabbie.model.Driver;
import com.app.cabbie.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RidesRepository extends JpaRepository<Ride,Long> {

    Optional<List<Ride>> findByPassengerId(Long userId);

    List<Ride> findByDriverId(Driver driverId);


}
