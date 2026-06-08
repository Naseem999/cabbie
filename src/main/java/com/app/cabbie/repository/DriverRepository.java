// Purpose: Spring Data JPA repository for database operations on Driver entities.
// Notes: Provides custom query methods for finding drivers by userId, status, or retrieving user IDs for drivers.

package com.app.cabbie.repository;

import com.app.cabbie.enums.DriverStatus;
import com.app.cabbie.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver,Long> {

    // Purpose: Retrieves a Driver by userId using native SQL query.
    // Behavior: Returns Optional<Driver>; executes raw SQL SELECT from drivers table.
    @Query(value = "Select * from drivers where user_id=:id;",nativeQuery = true)
    Optional<Driver> findEntityByUserId(@Param("id") Long id);

    // Purpose: Finds a Driver by the associated User ID using Spring Data JPA derived query.
    // Behavior: Uses JPQL query generation from method name; returns Optional<Driver>.
    Optional<Driver> findByUserId( Long id);

    // Purpose: Lists all Drivers with a specific DriverStatus (e.g., AVAILABLE, BUSY, OFFLINE).
    // Behavior: Returns List<Driver> matching the given status enum; used for ride assignment.
    List<Driver> findByDriverStatus(DriverStatus driverStatus);

    // Purpose: Retrieves the associated User ID for a given Driver ID via native SQL.
    // Behavior: Returns Long userIdId; direct database lookup without hydrating Driver entity.
    @Query(value = "select user_id from drivers where id=:driverId;", nativeQuery = true)
    Long findUserIdByDriverId(@Param("driverId") Long driverId);
}
