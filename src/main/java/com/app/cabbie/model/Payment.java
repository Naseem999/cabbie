package com.app.cabbie.model;

import com.app.cabbie.enums.PaymentMethod;
import com.app.cabbie.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", referencedColumnName = "id")
    private Ride rideId;

    @Column(name = "amount", columnDefinition = "DECIMAL(10,2)")
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 255)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 255)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_gateway_order_id", columnDefinition = "VARCHAR(255)")
    private String paymentGatewayOrderId;

    @Column(name = "payment_gateway_payment_id", columnDefinition = "VARCHAR(255)")
    private String paymentGatewayPaymentId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
