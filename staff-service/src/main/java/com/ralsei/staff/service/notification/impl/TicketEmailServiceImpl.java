package com.ralsei.staff.service.notification.impl;

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

import com.ralsei.staff.dto.notification.PassengerRefundCompletedEmailPayload;
import com.ralsei.staff.dto.notification.PassengerSeatEmailItem;
import com.ralsei.staff.dto.notification.PassengerTicketCancellationEmailPayload;
import com.ralsei.staff.dto.notification.PassengerTicketEmailPayload;
import com.ralsei.staff.service.notification.TicketEmailService;
import com.ralsei.staff.util.EmailUtility;
import com.ralsei.staff.util.QRCreateUitility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
            "Xác nhận vé " + payload.ticketCode() + " - Nhà xe Ralsei",
            htmlBody,
            inlineImages
        );
        log.info("Sent passenger ticket confirmation for ticketCode={}", payload.ticketCode());
    }

    @Override
    public void sendTicketUpdated(PassengerTicketEmailPayload payload) {
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

        String htmlBody = templateEngine.process("email/passenger-ticket-updated", context);
        emailUtility.sendHtml(
            payload.primaryEmail(),
            "Vé đã được cập nhật " + payload.ticketCode() + " - Nhà xe Ralsei",
            htmlBody,
            inlineImages
        );
        log.info("Sent passenger ticket update for ticketCode={}", payload.ticketCode());
    }

    @Override
    public void sendTicketCancellation(PassengerTicketCancellationEmailPayload payload) {
        validateCancellationPayload(payload);
        PassengerTicketEmailPayload ticket = payload.ticket();

        Context context = new Context(VIETNAMESE_LOCALE);
        context.setVariable("ticket", ticket);
        context.setVariable("cancellation", payload);
        context.setVariable("seatNumbers", ticket.seats().stream()
            .map(PassengerSeatEmailItem::seatCode)
            .toList());
        context.setVariable("coachNumber", extractLastFourDigits(ticket.coachLicensePlate()));
        context.setVariable("departureTime", formatDateTime(ticket.departureTime()));
        context.setVariable("arrivalTime", formatDateTime(ticket.arrivalTime()));
        context.setVariable("pickupTime", formatDateTime(ticket.pickupPresentBy()));
        context.setVariable("paidAt", formatDateTime(ticket.paidAt()));
        context.setVariable("cancelledAt", formatDateTime(payload.cancelledAt()));
        context.setVariable("formattedTotal", NumberFormat.getCurrencyInstance(VIETNAMESE_LOCALE)
            .format(ticket.totalPrice()));
        context.setVariable("formattedRefund", NumberFormat.getCurrencyInstance(VIETNAMESE_LOCALE)
            .format(payload.refundAmount()));

        String htmlBody = templateEngine.process("email/passenger-ticket-cancellation", context);
        emailUtility.sendHtml(
            ticket.primaryEmail(),
            "Thông báo hủy vé " + ticket.ticketCode() + " - Nhà xe Ralsei",
            htmlBody,
            Map.of()
        );
        log.info("Sent passenger ticket cancellation for ticketCode={}", ticket.ticketCode());
    }

    @Override
    public void sendRefundCompleted(PassengerRefundCompletedEmailPayload payload) {
        validateRefundCompletedPayload(payload);
        PassengerTicketEmailPayload ticket = payload.ticket();

        Context context = new Context(VIETNAMESE_LOCALE);
        context.setVariable("ticket", ticket);
        context.setVariable("refund", payload);
        context.setVariable("seatNumbers", ticket.seats().stream()
            .map(PassengerSeatEmailItem::seatCode)
            .toList());
        context.setVariable("coachNumber", extractLastFourDigits(ticket.coachLicensePlate()));
        context.setVariable("departureTime", formatDateTime(ticket.departureTime()));
        context.setVariable("arrivalTime", formatDateTime(ticket.arrivalTime()));
        context.setVariable("pickupTime", formatDateTime(ticket.pickupPresentBy()));
        context.setVariable("paidAt", formatDateTime(ticket.paidAt()));
        context.setVariable("refundTime", formatDateTime(payload.refundTime()));
        context.setVariable("formattedRefund", NumberFormat.getCurrencyInstance(VIETNAMESE_LOCALE)
            .format(payload.refundAmount()));
        context.setVariable("formattedTotal", NumberFormat.getCurrencyInstance(VIETNAMESE_LOCALE)
            .format(ticket.totalPrice()));

        String htmlBody = templateEngine.process("email/passenger-refund-completed", context);
        emailUtility.sendHtml(
            ticket.primaryEmail(),
            "Xác nhận hoàn tiền vé " + ticket.ticketCode() + " - Nhà xe Ralsei",
            htmlBody,
            Map.of()
        );
        log.info("Sent passenger refund completion for ticketCode={}", ticket.ticketCode());
    }

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

    private void validateRefundCompletedPayload(PassengerRefundCompletedEmailPayload payload) {
        if (payload == null || payload.ticket() == null) {
            throw new IllegalArgumentException("Dữ liệu email hoàn tiền không được để trống.");
        }
        validatePayload(payload.ticket());
        if (payload.refundAmount() == null) {
            throw new IllegalArgumentException("Email hoàn tiền phải có số tiền hoàn.");
        }
        if (payload.refundTime() == null) {
            throw new IllegalArgumentException("Email hoàn tiền phải có thời gian hoàn.");
        }
        if (payload.refundMethod() == null || payload.refundMethod().isBlank()) {
            throw new IllegalArgumentException("Email hoàn tiền phải có phương thức hoàn.");
        }
    }

    private void validateCancellationPayload(PassengerTicketCancellationEmailPayload payload) {
        if (payload == null || payload.ticket() == null) {
            throw new IllegalArgumentException("Dữ liệu email hủy vé không được để trống.");
        }
        validatePayload(payload.ticket());
        if (payload.refundAmount() == null) {
            throw new IllegalArgumentException("Email hủy vé phải có số tiền hoàn.");
        }
        if (payload.refundStatus() == null || payload.refundStatus().isBlank()) {
            throw new IllegalArgumentException("Email hủy vé phải có trạng thái hoàn tiền.");
        }
    }

    private String formatDateTime(java.time.LocalDateTime value) {
        return value == null ? "Đang cập nhật" : value.format(DATE_TIME_FORMATTER);
    }

    private String extractLastFourDigits(String licensePlate) {
        if (licensePlate == null || licensePlate.isBlank()) {
            return "đang cập nhật";
        }
        String digits = licensePlate.replaceAll("\\D", "");
        return digits.length() <= 4 ? digits : digits.substring(digits.length() - 4);
    }

    private record SeatEmailView(
        String seatCode,
        String passengerName,
        String passengerPhone,
        String qrContentId
    ) {}
}
