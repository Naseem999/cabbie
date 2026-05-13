package com.app.cabbie.model;

import com.app.cabbie.enums.DriverStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name ="drivers")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , referencedColumnName = "id")
    private User user;

    @Column(name = "vehicle_model", columnDefinition = "VARCHAR(50)")
    private String vechicalModel;

    @Column(name = "vehicle_number", columnDefinition = "VARCHAR(20)")
    private String vechicalNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 255)
    private DriverStatus driverStatus;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
