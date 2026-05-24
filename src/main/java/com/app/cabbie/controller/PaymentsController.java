package com.app.cabbie.controller;

import com.app.cabbie.dto.PaymentDetailsDTO;
import com.app.cabbie.model.Payment;
import com.app.cabbie.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentsController {

    @Autowired
    PaymentService paymentService;

    @PostMapping("/pay")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Object> makePayment(@RequestBody PaymentDetailsDTO paymentDetailsDTO){
        try {
           Map<String,Object> paymentObject= paymentService.createOrder(paymentDetailsDTO.getRideId(),paymentDetailsDTO.getAmount());
           return ResponseEntity.status(HttpStatus.CREATED).body(paymentObject);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
        }
    }


    @PostMapping("/verify")
    public ResponseEntity<Object> verifyPayment(@RequestBody Map<String,String> verifyPaymentMap){
        try {
            String rPaymentId=verifyPaymentMap.get("razorpay_payment_id");
            String rOrderId=verifyPaymentMap.get("razorpay_order_id");
            String rSignature=verifyPaymentMap.get("razorpay_signature");
            Map<String,Object> paymentObject= paymentService.verifyPayment(rOrderId,rPaymentId,rSignature);
            return ResponseEntity.status(HttpStatus.CREATED).body(paymentObject);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
        }
    }


    @GetMapping("/user/{passengerId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<List<Payment>> getAllPaymentsForPassenger(@PathVariable Long passengerId){
       List<Payment> payments= paymentService.getAllPaymentsForPassenger(passengerId);
       return ResponseEntity.ok(payments);
    }

    @GetMapping("/earnings/{driverId}")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<Payment>> getAllPaymentsForDriver(@PathVariable Long driverId){
        List<Payment> payments= paymentService.getAllPaymentsForDriver(driverId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Payment>> getAllPayments(){
        List<Payment> payments= paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

}
