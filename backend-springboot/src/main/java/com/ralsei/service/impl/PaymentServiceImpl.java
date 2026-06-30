package com.ralsei.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ralsei.dto.request.payment.PaymentCheckoutRequest;
import com.ralsei.dto.request.sePay.SepayWebhookRequest;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.model.PassengerTicket;
import com.ralsei.model.Payment;
import com.ralsei.model.enums.PassengerTicketDetailStatus;
import com.ralsei.model.enums.PassengerTicketStatus;
import com.ralsei.model.enums.TripSeatStatus;
import com.ralsei.repository.PassengerTicketDetailRepository;
import com.ralsei.repository.PassengerTicketRepository;
import com.ralsei.repository.PaymentRepository;
import com.ralsei.repository.TripSeatRepository;
import com.ralsei.service.PaymentService;
import com.ralsei.service.TransactionIdGenerator;
import com.ralsei.service.VoucherService;
import com.ralsei.service.passengerbooking.PaymentSseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PassengerTicketRepository passengerTicketRepository;
    private final PassengerTicketDetailRepository passengerTicketDetailRepository;
    private final TripSeatRepository tripSeatRepository;
    private final ObjectMapper objectMapper;
    private final TransactionIdGenerator transactionIdGenerator;
    private final PaymentSseService paymentSseService;
    private final VoucherService voucherService;

    @Override
    @Transactional
    public Payment initializePayment(PaymentCheckoutRequest request) {
        validateCheckoutRequest(request);

        String transactionId = transactionIdGenerator.generateUniqueTransactionId();

        Payment payment = Payment.builder()
                .passengerTicketId(request.getPassengerTicketId())
                .cargoTicketId(request.getCargoTicketId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(transactionId)
                .status("PENDING")
                .refundAmount(BigDecimal.ZERO)
                .build();

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public void processWebhook(SepayWebhookRequest request) {
        String content = request.getContent();
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Webhook content is empty");
        }

        // Extract transactionId from content (e.g., PAYxxxxxx)
        Pattern pattern = Pattern.compile("(PAY[A-Z0-9]{6})");
        Matcher matcher = pattern.matcher(content);

        String transactionId = null;
        if (matcher.find()) {
            transactionId = matcher.group(1);
        }

        if (transactionId == null) {
            throw new IllegalArgumentException("Could not extract transactionId from content");
        }

        Optional<Payment> paymentOpt = paymentRepository.findByTransactionIdAndStatus(transactionId, "PENDING");
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();

            // Verify transfer amount
            if (payment.getAmount().compareTo(request.getTransferAmount()) <= 0) {
                payment.setStatus("COMPLETED");
                payment.setPaymentTime(LocalDateTime.now());

                try {
                    payment.setCallbackData(objectMapper.writeValueAsString(request));
                } catch (JsonProcessingException e) {
                    payment.setCallbackData(request.toString());
                }

                paymentRepository.save(payment);

                if (payment.getPassengerTicketId() != null) {
                    completePassengerPaymentTarget(payment);
                } else if (payment.getCargoTicketId() != null) {
                    completeCargoPaymentTarget(payment); 
                } else {
                    throw new BusinessRuleException("Dữ liệu thanh toán không hợp lệ!");
                }

                paymentSseService.sendStatusUpdate(transactionId, payment.getStatus());

            } else {
                throw new IllegalArgumentException("Transfer amount is less than required payment amount");
            }
        } else {
            throw new IllegalArgumentException(
                    "Payment not found or already processed for transactionId: " + transactionId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Không tìm thấy thanh toán có mã giao dịch: " + transactionId));
    }

    @Override
    @Transactional
    public void cancelPayment(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Không tìm thấy thanh toán có mã giao dịch: " + transactionId));
        if (!"COMPLETED".equals(payment.getStatus())) {
            payment.setStatus("FAILED");
            paymentRepository.save(payment);
            
            if (payment.getPassengerTicketId() != null) {
                cancelPassengerPaymentTarget(payment);
            } else if (payment.getCargoTicketId() != null) {
                cancelCargoPaymentTarget(payment); 
            } else {
                throw new BusinessRuleException("Dữ liệu thanh toán không hợp lệ!");
            }

            paymentSseService.sendStatusUpdate(transactionId, payment.getStatus());
        }
    }

    private void validateCheckoutRequest(PaymentCheckoutRequest request) {
        if (request == null) {
            throw new BusinessRuleException("Thông tin thanh toán không được để trống!");
        }
        boolean hasPassengerTicket = request.getPassengerTicketId() != null;
        boolean hasCargoTicket = request.getCargoTicketId() != null;
        if (hasPassengerTicket == hasCargoTicket) {
            throw new BusinessRuleException("Một thanh toán chỉ được ứng với vé hành khách hoặc vé hàng hóa!");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Số tiền thanh toán phải lớn hơn 0!");
        }
        if (request.getPaymentMethod() == null || request.getPaymentMethod().isBlank()) {
            throw new BusinessRuleException("Phải chọn phương thức thanh toán!");
        }
    }

    private void completePassengerPaymentTarget(Payment payment) {
        int rowsAffected = passengerTicketRepository.updateStatusIfCurrent(
                payment.getPassengerTicketId(),
                PassengerTicketStatus.PENDING,
                PassengerTicketStatus.CONFIRMED);

        if (rowsAffected == 0) {
            throw new BusinessRuleException("Thao tác thất bại: Vé không tồn tại hoặc trạng thái vé đã thay đổi trước đó!");
        }

        passengerTicketDetailRepository.updateStatusByPassengerTicketId(
                payment.getPassengerTicketId(),
                PassengerTicketDetailStatus.CONFIRMED.name());

        List<Integer> tripSeatIds = passengerTicketDetailRepository
                .findTripSeatIdsByPassengerTicketId(payment.getPassengerTicketId());
        if (!tripSeatIds.isEmpty()) {
            tripSeatRepository.updateStatusByTripSeatIds(tripSeatIds, TripSeatStatus.SOLD);
        }
    }

    private void cancelPassengerPaymentTarget(Payment payment) {
        passengerTicketRepository.updateStatusIfCurrent(
                payment.getPassengerTicketId(),
                PassengerTicketStatus.PENDING,
                PassengerTicketStatus.CANCELLED);

        passengerTicketDetailRepository.updateStatusByPassengerTicketId(
                payment.getPassengerTicketId(),
                PassengerTicketDetailStatus.EXPIRED.name());

        List<Integer> tripSeatIds = passengerTicketDetailRepository
                .findTripSeatIdsByPassengerTicketId(payment.getPassengerTicketId());
        if (!tripSeatIds.isEmpty()) {
            tripSeatRepository.updateStatusByTripSeatIds(tripSeatIds, TripSeatStatus.AVAILABLE);
        }

        passengerTicketRepository.findById(payment.getPassengerTicketId()).ifPresent(ticket -> {
            releaseVoucherUsageIfApplied(ticket);
        });
    }

    private void releaseVoucherUsageIfApplied(PassengerTicket ticket) {
        if (ticket.getVoucherId() == null || ticket.getVoucherCodeSnapshot() == null) {
            return;
        }
        int rowsUpdated = voucherService.decrementUsedCountIfApplied(ticket.getVoucherId());
        if (rowsUpdated == 0) {
            log.warn("Không thể hoàn lượt voucher id={} khi hủy vé {}", ticket.getVoucherId(), ticket.getTicketCode());
        }
    }

    private void completeCargoPaymentTarget(Payment payment) {
        return;
    }

    private void cancelCargoPaymentTarget(Payment payment) {
        return;
    }
}
