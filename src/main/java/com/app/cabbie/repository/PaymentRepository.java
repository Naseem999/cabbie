package com.app.cabbie.repository;

import com.app.cabbie.dto.PaymentDetailsDTO;
import com.app.cabbie.model.Payment;
import com.app.cabbie.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRideId(Long rideId);

    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
    Optional<Payment> findByPaymentGatewayOrderId(String orderId);


    @Query(value ="Select * from payments p join rides r on p.rideId=r.id and r.passenger_id = :passengerId;", nativeQuery = true)
    List<Payment> getPaymentDetailsFromPassengerId(@Param("passengerId") Long passengerId);


    @Query(value ="Select * from payments p join rides r on p.rideId=r.id and r.driver_id = :driverId;", nativeQuery = true)
    List<Payment> getPaymentDetailsFromDriverId(@Param("driverId") Long driverId);




}

