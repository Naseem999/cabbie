package com.app.cabbie.repository;

import com.app.cabbie.model.Payment;
import com.app.cabbie.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRideId(Long rideId);

    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
}

