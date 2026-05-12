package com.app.cabbie.repository;

import com.app.cabbie.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RidesRepository extends JpaRepository<Ride,Long> {
}
