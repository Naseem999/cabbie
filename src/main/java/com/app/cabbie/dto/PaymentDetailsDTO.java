package com.app.cabbie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor

public class PaymentDetailsDTO {
    private Long rideId;
    private BigDecimal amount;
}
