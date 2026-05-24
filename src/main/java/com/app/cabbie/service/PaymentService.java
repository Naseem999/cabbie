package com.app.cabbie.service;

import com.app.cabbie.enums.PaymentMethod;
import com.app.cabbie.enums.PaymentStatus;
import com.app.cabbie.model.Payment;
import com.app.cabbie.model.Ride;
import com.app.cabbie.repository.PaymentRepository;
import com.app.cabbie.repository.RidesRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PaymentService handles creation, verification and retrieval of payments.
 * Uses Razorpay for order creation and signature verification.
 */
@Service
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Autowired
    private  RazorpayClient razorpayClient;

    @Autowired
    RidesRepository ridesRepository;

    @Autowired
    PaymentRepository paymentRepository;



    public Map<String, Object> createOrder(Long rideId, BigDecimal amount)throws RazorpayException {

        int paise= amount.multiply(BigDecimal.valueOf(100)).intValue();

        JSONObject options = new JSONObject();
        options.put("amount",paise);
        options.put("currency","INR");
        options.put("receipt","ride_"+rideId);

        Order rzpOrder=razorpayClient.orders.create(options);
        Ride ride=ridesRepository.findById(rideId).orElseThrow(()-> new RuntimeException("Ride Not Found"));
        Payment payment= Payment.builder()
                .rideId(ride)
                .paymentStatus(PaymentStatus.UNPAID)
                .amount(amount.doubleValue())
                .paymentGatewayOrderId(rzpOrder.get("id"))
                .build();

        paymentRepository.save(payment);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId",  rzpOrder.get("id"));
        response.put("amount",   paise);
        response.put("currency", "INR");
        response.put("keyId",    keyId);
        return response;
        
    }

    public Map<String, Object> verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature){
        String data= razorpayOrderId+"|"+razorpayPaymentId;
        try {
            String generatedSignature=hmac(data,keySecret);

            Payment payment=paymentRepository.findByPaymentGatewayOrderId(razorpayOrderId).orElseThrow(()-> new RuntimeException("Payment With Order Id :"+razorpayOrderId+" not found."));
            if(!generatedSignature.equals(razorpaySignature)){
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                return Map.of(
                        "status",  "FAILED",
                        "message", "Invalid signature — payment rejected"
                );
            }

            // Fetch payment details from Razorpay
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            com.razorpay.Payment rzpPayment = client.payments.fetch(razorpayPaymentId);

            // Step 3: Extract payment method
            String method = rzpPayment.get("method");

            System.out.println("RAW METHOD FROM RAZORPAY: '" + method + "'");

            String paymentMethod = mapMethod(method);

            System.out.println("RAW METHOD FROM RAZORPAY: '" + paymentMethod + "'");

            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaymentMethod(PaymentMethod.valueOf(paymentMethod));
            payment.setPaymentGatewayPaymentId(razorpayPaymentId);
            paymentRepository.save(payment);

            return  Map.of("status","PAID",
                       "message","Payment Successful",
                        "Payment Id" ,razorpayPaymentId);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }


    // ─────────────────────────────────────────────
    // HMAC helper — generates the signature
    // ─────────────────────────────────────────────
    private String hmac(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private String mapMethod(String method){
        if (method == null) return "MOCK";
        return switch (method.toLowerCase().trim()) {
            case "upi"        -> "UPI";
            case "card"       -> "CREDIT_CARD";
            case "netbanking" ->"NET_BANKING";  // ← handles both
            default           -> "MOCK";
        };
    }



    public List<Payment> getAllPaymentsForPassenger(Long passengerId){
       return paymentRepository.getPaymentDetailsFromPassengerId(passengerId);
    }

    public List<Payment> getAllPaymentsForDriver(Long driverId){
        return paymentRepository.getPaymentDetailsFromDriverId(driverId);
    }

    public List<Payment> getAllPayments(){
        return paymentRepository.findAll();
    }


}
