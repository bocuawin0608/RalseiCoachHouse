package com.ralsei.service.notification.impl;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.ralsei.dto.notification.PassengerSeatEmailItem;
import com.ralsei.dto.notification.PassengerTicketEmailPayload;
import com.ralsei.service.notification.TicketEmailService;
import com.ralsei.util.EmailUtility;
import com.ralsei.util.QRCreateUitility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Renders the customer-facing passenger ticket template and sends it through
 * the shared mail utility. Sensitive boarding tokens are converted into inline
 * QR images and are never inserted as visible template text.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketEmailServiceImpl implements TicketEmailService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy");
    private static final Locale VIETNAMESE_LOCALE = Locale.forLanguageTag("vi-VN");

    private final TemplateEngine templateEngine;
    private final EmailUtility emailUtility;
    private final QRCreateUitility qrCreateUitility;

    /**
     * Creates one readable confirmation email for the primary passenger, with
     * a separate QR image for every booked seat.
     *
     * @param payload complete paid-ticket data
     */
    @Override
    public void sendTicketConfirmation(PassengerTicketEmailPayload payload) {
        validatePayload(payload);

        Map<String, byte[]> inlineImages = new LinkedHashMap<>();
        List<SeatEmailView> seatViews = new ArrayList<>();
        for (int index = 0; index < payload.seats().size(); index++) {
            PassengerSeatEmailItem seat = payload.seats().get(index);
            String qrContentId = null;
            if (seat.boardingToken() != null && !seat.boardingToken().isBlank()) {
                qrContentId = "boarding-qr-" + index;
                inlineImages.put(qrContentId, qrCreateUitility.createPng(seat.boardingToken()));
            }
            seatViews.add(new SeatEmailView(
                seat.seatCode(),
                seat.passengerName(),
                seat.passengerPhone(),
                qrContentId
            ));
        }

        Context context = new Context(VIETNAMESE_LOCALE);
        context.setVariable("ticket", payload);
        context.setVariable("seats", seatViews);
        context.setVariable("seatNumbers", payload.seats().stream()
            .map(PassengerSeatEmailItem::seatCode)
            .toList());
        context.setVariable("coachNumber", extractLastFourDigits(payload.coachLicensePlate()));
        context.setVariable("departureTime", formatDateTime(payload.departureTime()));
        context.setVariable("arrivalTime", formatDateTime(payload.arrivalTime()));
        context.setVariable("pickupTime", formatDateTime(payload.pickupPresentBy()));
        context.setVariable("paidAt", formatDateTime(payload.paidAt()));
        context.setVariable("formattedTotal", NumberFormat.getCurrencyInstance(VIETNAMESE_LOCALE)
            .format(payload.totalPrice()));

        String htmlBody = templateEngine.process("email/passenger-ticket-confirmation", context);
        emailUtility.sendHtml(
            payload.primaryEmail(),
            "Xác nhận vé " + payload.ticketCode() + " - Nhà xe Tuấn MV",
            htmlBody,
            inlineImages
        );
        log.info("Sent passenger ticket confirmation for ticketCode={}", payload.ticketCode());
    }

    /** Ensures template-required fields are present before contacting SMTP. */
    private void validatePayload(PassengerTicketEmailPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Dữ liệu email vé không được để trống.");
        }
        if (payload.primaryEmail() == null || payload.primaryEmail().isBlank()) {
            throw new IllegalArgumentException("Vé không có email người nhận hợp lệ.");
        }
        if (payload.ticketCode() == null || payload.ticketCode().isBlank()) {
            throw new IllegalArgumentException("Vé không có mã xác nhận hợp lệ.");
        }
        if (payload.totalPrice() == null || payload.seats() == null || payload.seats().isEmpty()) {
            throw new IllegalArgumentException("Vé không có đầy đủ thông tin giá hoặc ghế.");
        }
    }

    /** Formats optional schedule timestamps consistently throughout the email. */
    private String formatDateTime(java.time.LocalDateTime value) {
        return value == null ? "Đang cập nhật" : value.format(DATE_TIME_FORMATTER);
    }

    /**
     * Returns the last four numeric plate characters shown to the customer.
     * For example, {@code 51B-334.49} becomes {@code 3449}.
     */
    private String extractLastFourDigits(String licensePlate) {
        if (licensePlate == null || licensePlate.isBlank()) {
            return "đang cập nhật";
        }
        String digits = licensePlate.replaceAll("\\D", "");
        return digits.length() <= 4 ? digits : digits.substring(digits.length() - 4);
    }

    /** Template-only representation that exposes an inline QR content ID. */
    private record SeatEmailView(
        String seatCode,
        String passengerName,
        String passengerPhone,
        String qrContentId
    ) {}
}
