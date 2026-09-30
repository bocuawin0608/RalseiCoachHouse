package com.ralsei.payment.service.impl;

import com.ralsei.payment.dto.PaymentRequest;
import com.ralsei.payment.dto.PaymentResponse;
import com.ralsei.payment.service.PaymentService;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

@Service
public class PaymentServiceImpl implements PaymentService {

    // Dummy logic to generate VNPay URL structure without external non-standard libs
    @Override
    public PaymentResponse createVNPayPayment(PaymentRequest request) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = request.getOrderId();
        
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());

        // Basic URL builder simulation
        String paymentUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?" +
                "vnp_Version=" + vnp_Version + 
                "&vnp_Command=" + vnp_Command +
                "&vnp_TxnRef=" + vnp_TxnRef + 
                "&vnp_Amount=" + (request.getAmount() * 100) +
                "&vnp_CreateDate=" + vnp_CreateDate;

        return PaymentResponse.builder()
                .status("OK")
                .message("Successfully created payment URL")
                .paymentUrl(paymentUrl)
                .build();
    }
}
