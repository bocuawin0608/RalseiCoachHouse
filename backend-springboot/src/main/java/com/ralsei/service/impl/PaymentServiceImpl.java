package com.ralsei.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ralsei.dto.request.payment.PaymentCheckoutRequest;
import com.ralsei.dto.request.sePay.SepayWebhookRequest;
import com.ralsei.dto.notification.PassengerTicketEmailPayload;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.model.CargoTicket;
import com.ralsei.model.PassengerTicket;
import com.ralsei.model.PassengerTicketDetail;
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
import com.ralsei.service.notification.PassengerTicketEmailAssembler;
import com.ralsei.service.notification.TicketEmailService;
import com.ralsei.service.passengerbooking.BoardingQrTokenGenerator;
import com.ralsei.service.passengerbooking.PaymentSseService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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
    private final BoardingQrTokenGenerator boardingQrTokenGenerator;
    private final PassengerTicketEmailAssembler passengerTicketEmailAssembler;
    private final TicketEmailService ticketEmailService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Payment initializePayment(PaymentCheckoutRequest request) {
        validateCheckoutRequest(request);

        String transactionId = transactionIdGenerator.generateUniqueTransactionId();

        CargoTicket ct = request.getCargoTicketId() != null
                ? entityManager.getReference(CargoTicket.class, request.getCargoTicketId())
                : null;

        Payment payment = Payment.builder()
                .passengerTicketId(request.getPassengerTicketId())
                .cargoTicket(ct)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(transactionId)
                .cancelToken(UUID.randomUUID().toString())
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
                String callbackData;
                try {
                    callbackData = objectMapper.writeValueAsString(request);
                } catch (JsonProcessingException e) {
                    callbackData = request.toString();
                }

                LocalDateTime paymentTime = LocalDateTime.now();
                int rowsUpdated = paymentRepository.completeIfCurrent(
                        transactionId,
                        "PENDING",
                        "COMPLETED",
                        paymentTime,
                        callbackData);

                if (rowsUpdated == 0) {
                    log.info("Payment already processed before webhook completion, transactionId={}", transactionId);
                    return;
                }

                // completeIfCurrent evicts the managed entity; sync in-memory copy for
                // downstream use only.
                payment.setStatus("COMPLETED");
                payment.setPaymentTime(paymentTime);
                payment.setCallbackData(callbackData);

                if (payment.getPassengerTicketId() != null) {
                    completePassengerPaymentTarget(payment);
                } else if (payment.getCargoTicket().getCargoTicketId() > 0) {
                    completeCargoPaymentTarget(payment);
                } else {
                    throw new BusinessRuleException("Dữ liệu thanh toán không hợp lệ!");
                }

                paymentSseService.sendStatusUpdate(transactionId, "COMPLETED");

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
                        () -> new IllegalArgumentException(
                                "Không tìm thấy thanh toán có mã giao dịch: " + transactionId));
    }

    @Override
    @Transactional
    public boolean failPendingPayment(String transactionId) {
        int rowsUpdated = paymentRepository.updateStatusIfCurrent(transactionId, "PENDING", "FAILED");
        if (rowsUpdated == 0) {
            log.info("Skip fail pending payment because payment is no longer pending, transactionId={}", transactionId);
            return false;
        }

        paymentSseService.sendStatusUpdate(transactionId, "FAILED");
        return true;
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
            throw new BusinessRuleException(
                    "Thao tác thất bại: Vé không tồn tại hoặc trạng thái vé đã thay đổi trước đó!");
        }

        List<PassengerTicketDetail> details = passengerTicketDetailRepository
                .findByPassengerTicketId(payment.getPassengerTicketId());

        if (!details.isEmpty()) {
            List<Integer> tripSeatIds = new ArrayList<>();

            for (PassengerTicketDetail detail : details) {
                detail.setStatus(PassengerTicketDetailStatus.CONFIRMED.name());
                detail.setQrcode(boardingQrTokenGenerator.generateToken());

                if (detail.getTripSeatId() > 0) {
                    tripSeatIds.add(detail.getTripSeatId());
                }
            }

            passengerTicketDetailRepository.saveAll(details);

            if (!tripSeatIds.isEmpty()) {
                tripSeatRepository.updateStatusByTripSeatIds(tripSeatIds, TripSeatStatus.SOLD);
            }
        }

        PassengerTicketEmailPayload emailPayload = passengerTicketEmailAssembler
                .assemble(payment.getPassengerTicketId());
        sendTicketEmailAfterCommit(emailPayload, payment.getPassengerTicketId());
    }

    /**
     * Defers customer communication until the enclosing payment transaction has
     * committed. This prevents a rollback from leaving the customer with an
     * email for a ticket that was never successfully confirmed.
     *
     * @param payload           detached ticket information safe to use after commit
     * @param passengerTicketId identifier used only for failure diagnostics
     */
    private void sendTicketEmailAfterCommit(
            PassengerTicketEmailPayload payload,
            Integer passengerTicketId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    ticketEmailService.sendTicketConfirmation(payload);
                } catch (Exception exception) {
                    log.error("Failed to send ticket confirmation for passengerTicketId={}",
                            passengerTicketId, exception);
                }
            }
        });
    }

    private void completeCargoPaymentTarget(Payment payment) {
        return;
    }
}
